/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitorudp;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
    Faz monitorização dos pacotes de tipo 0.
    Actualiza pacotes perdidos.
  
*/
public class Monitor implements Runnable {
    private ArrayBlockingQueue<byte[]> packets;
    private ServerStatus server;
    private ConcurrentSkipListSet<ServerStatus> table;
    private InetAddress ClIP;
    private int prevSeqNum;
    private HashMap<InetAddress,ArrayBlockingQueue> queues;
    private UserInput ui;
    
    private int timeout,timeRem;
    
    public Monitor (ConcurrentSkipListSet<ServerStatus> t,ArrayBlockingQueue p,ServerStatus s,InetAddress ClIP,HashMap h,UserInput u,int timeout,int timeRem){
        this.server=s;
        this.packets=p;
        this.table=t;
        this.ClIP=ClIP;
        this.queues=h;
        this.ui=u;
        this.timeout=timeout;
        this.timeRem=timeRem;
        prevSeqNum=0;
    }
    
    @Override
    public void run(){
        boolean timedOut=false,active=true;
        int seqNum = 0;
        
        //Enquanto o utilizador não tiver terminado e o servidor não ter sido removido da tabela...
        while(active&&!ui.getQuit()){
            byte[] receiveData=null;
            boolean correctPacket=false;
            try {
                /*
                    Se ainda não houve timeout, fica à espera por um pacote até ao tempo de timeout tiver passado
                */
                if (!timedOut){
                    while(!correctPacket){
                        receiveData = packets.poll(timeout,TimeUnit.SECONDS);
                        correctPacket=true;
                        if (receiveData!=null&&receiveData[0]==1) {correctPacket=false;prevSeqNum=++seqNum;}
                    }
                }else{
                    /*
                        Se houve timeout, espera novamente por outro pacote.
                        Caso não o receba no tempo definido, o servidor é removido.
                    */
                    receiveData = packets.poll(timeRem,TimeUnit.SECONDS);
                    if(receiveData!=null){
                        timedOut=false;
                    }else{
                        table.remove(server);
                        queues.remove(ClIP);
                        active=false;
                        System.out.println("Server with IP "+server.getIP()+" got removed for inactivity.");
                    }
                }
                if(active&&!ui.getQuit()){
                    if(receiveData!=null){
                        //ler número de sequencia
                        seqNum=receiveData[1];
                        if(seqNum!=0){
                            /*
                                    Caso haja perda de pacotes.
                                    Há perdas quando diferença absoluta entre o número de sequencia anteriormente recebido e numero de sequencia
                                do pacote é maior que 1 ou são iguais/seqNum tem menos uma unidade do que prevSeqNum (número máximo de perdas 
                                consecutivas é 99)
                            */
                            if (Math.abs(seqNum-prevSeqNum)>1||(seqNum==prevSeqNum||seqNum==prevSeqNum-1)){
                                if (seqNum>prevSeqNum){
                                    synchronized(table){
                                        table.remove(server);
                                        server.updatePL(seqNum-prevSeqNum-1);
                                        table.add(server);
                                    }
                                }else{
                                    synchronized(table){
                                        table.remove(server);
                                        server.updatePL((100-prevSeqNum)+seqNum-1);
                                        table.add(server);
                                    }
                                }
                            }
                            prevSeqNum=seqNum;
                        }else{
                            /*
                                se número de sequencia recebido for 0, reiniciar contagem de pacotes perdidos (apenas acontece quando servidor
                                backend vai a baixo e consegue voltar a connectar antes de ser removido da tabela.
                            */
                            prevSeqNum=0;
                            synchronized(table){
                                table.remove(server);
                                server.resetPL();
                                table.add(server);
                            }
                        }                            
                    }else{
                        //se o pacote recebido for nulo (timeout), a entrada do servidor na tabela deixa de ser válida.
                        synchronized(table){
                            table.remove(server);
                            server.setValid(0);
                            table.add(server);
                        }
                        System.out.println("Server with IP "+server.getIP()+" timed out.");
                        timedOut=true;
                    }
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Monitor.class.getName()).log(Level.SEVERE, null, ex);
            }                
        }
        //quando execução termina, remover servidor
        if (table.contains(server))table.remove(server);
    }
}
