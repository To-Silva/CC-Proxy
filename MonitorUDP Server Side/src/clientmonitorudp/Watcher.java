
package clientmonitorudp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
    Classe que fica à escuta na socket UDP, processando polls do proxy
 */
public class Watcher implements Runnable {
    
    private final PacketInfo pi;
    private UserInput ui;
    private DatagramSocket serverSocket;
    
    public Watcher(PacketInfo pi,UserInput u,DatagramSocket s){
        this.pi=pi;
        this.serverSocket=s;
        this.ui=u;
    }
    
    @Override
    public void run() {
        boolean initialized=false;
        try{
            //Enquanto não for executado o comando 'quit' pelo utilizador
            while(!ui.getQuit()){

                byte[] receiveData= new byte[2];
                //Receber pacote UDP
                DatagramPacket receivePacket= new DatagramPacket(receiveData,receiveData.length);
                serverSocket.receive(receivePacket);

                receiveData = receivePacket.getData();
                int i2 = receiveData[0] & 0xFF;
                //Se o pacote recebido for de poll
                if (i2==0){
                    System.out.println("received poll");
                    synchronized(pi){
                        //Se ainda não foi conseguida a ligação anteriormente, iniciar o processo de envio de pacotes normal
                        if (!initialized){
                            pi.setSN(1);
                            initialized=true;
                        }else{
                            pi.setPoll(true);
                            pi.notify(); 
                        }
                    }
                }
            }
        } catch (SocketException  | UnknownHostException ex) {
            
        } catch (IOException ex) {
            Logger.getLogger(Watcher.class.getName()).log(Level.SEVERE, null, ex);
        }      
    }
}
