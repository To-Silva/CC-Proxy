/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitorudp;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
/*
    Tipo de pacotes enviados:

                    Tipo de pacote 0 (registo periodico)
                    -Se SEQNUM=0
                        TIPO | SEQNUM | IP | BENCHM
                    -Se SEQNUM!=0
                        TIPO | SEQNUM |IP

                    Tipo de pacote 1 (depois de feito poll pelo proxy)
                        TIPO | CPUL | IP
*/

public class ClientMonitorUDP {
    private static String ipName;
    private static long pingFreq;
    
      //Função que verifica se o IP introduzido é válido.
    
    public static boolean checkIP(String ip) {
        if (ip == null || ip.isEmpty()) return false;
        ip = ip.trim();
        if ((ip.length() < 6) & (ip.length() > 15)) return false;

        try {
            Pattern pattern = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
            Matcher matcher = pattern.matcher(ip);
            return matcher.matches();
        } catch (PatternSyntaxException ex) {
            return false;
        }
    }    
      //Função que recebe os parametros iniciais  
    public static void getInputs(){
        boolean valid=false;
        Scanner in = new Scanner (System.in);
        while(!valid){        
            System.out.println("Insert proxy IP:");
            ipName=in.nextLine();
            valid=checkIP(ipName);
            if(!valid) System.out.println("Invalid IP address.");
        }
        valid=false;
        while(!valid){        
            System.out.println("Insert ping frequency (ms):");
            pingFreq=in.nextLong();
            valid=pingFreq>0;
            if (!valid) System.out.println("Invalid ping frequency.");
        }        
        
    }
    
    public static void main(String[] args) throws SocketException, IOException, InterruptedException {
        double cpuLD;
        int i;
        //Classe utilizada para guardar dados sobre input enquanto processo decorre
        UserInput input= new UserInput();
        //Sockets UDP
        DatagramSocket watcherSocket= new DatagramSocket(5555);
        DatagramSocket clientSocket = new DatagramSocket();
        
        getInputs();
        //Obter IP do proxy
        InetAddress IPAddress = InetAddress.getByName(ipName);
        //Obter IP do próprio backend
        InetAddress host = InetAddress.getLocalHost(); 
        String IPad=host.toString().replaceAll(".*/", "");
        System.out.println(host);
        byte[] ip= IPad.getBytes();
        
        //Cálculo do benchmark
        long startTime = System.nanoTime();
        Thread t = new Thread(new Benchmark()); 
        t.start();
        t.join();
        int benchmarkScore = (int)(100/((System.nanoTime() - startTime)*10E-10));
        
        System.out.println("Score: "+benchmarkScore);
        //Classe onde se guardam informações sobre o pacote a ser enviado 
        PacketInfo pi=new PacketInfo();
        
        //Iniciar threads de escuta para o input da socket e input IO 
        System.out.println("\n\tType 'quit' to exit the program.\n");
        Thread watcher = new Thread(new Watcher(pi,input,watcherSocket));
        watcher.start();
        Thread inputScanner= new Thread(new InputScanner(input));
        inputScanner.start();
        
        //Enquanto não for executado o comando 'quit' pelo utilizador
        while (!input.getQuit()) {
            synchronized(pi){
                i=2;
                //Se o próximo número de pacote for 101, fazer reset (números de sequência variam entre 0 e 100
                if (pi.getSN()==101) {pi.setSN(1);}

                //Se o proxy lançou poll
                if (pi.getPoll()){
                    byte[] sendData = new byte[ip.length+2];
                    //Obter CPU load
                    OperatingSystemMXBean osBean=(OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
                    cpuLD=((osBean.getSystemCpuLoad())*100);
                    
                    //Tipo de pacote 1
                    sendData[0]=1;
                    sendData[1]=(byte)cpuLD;
                    //Escrever IP do backend no array de bytes
                    for(byte b : ip){
                        sendData[i]=b;
                        i++;
                    }
                    //Enviar pacote
                    DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,IPAddress,5555);             
                    clientSocket.send(sendPacket);       
                    pi.setPoll(false);
                }else{
                    //Se for o pacote de request de ligação
                    if(pi.getSN()==0) {
                        byte[] sendData = new byte[ip.length+3];
                        System.out.println("Sequence number: "+pi.getSN());
                        //Tipo de pacote 0
                        sendData[0]= 0;
                        //Número de sequência = 0
                        sendData[1]=(byte) 0;
                        //Escrever IP do backend no array de bytes
                        for(byte b : ip){
                            sendData[i]=b;
                            i++;
                        }   
                        //Escrever benchmark
                        sendData[i]=(byte)benchmarkScore;    
                        //Enviar pacote
                        DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,IPAddress,5555);             
                        clientSocket.send(sendPacket);               
                    }else{
                        //Se for um pacote de registo normal
                        byte[] sendData = new byte[ip.length+2];
                        System.out.println("Sequence number: "+pi.getSN());
                        //Tipo de pacote 0
                        sendData[0]= 0;
                        //Número de sequência
                        sendData[1]=(byte) pi.getSN();
                        //Escrever IP do backend no array de bytes
                        for(byte b : ip){
                            sendData[i]=b;
                            i++;
                        }        
                        //Enviar pacote
                        DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,IPAddress,5555);             
                        clientSocket.send(sendPacket); 
                        pi.setSN(pi.getSN()+1);                          
                    }
                }
                //esperar por um poll ou esperar o valor de tempo definido para a frequência de ping
                if (!pi.getPoll()) pi.wait(pingFreq);
            }
        }
        //fechar sockets e esperar por threads
        watcherSocket.close();
        watcher.join();
        inputScanner.join();
    }    
}