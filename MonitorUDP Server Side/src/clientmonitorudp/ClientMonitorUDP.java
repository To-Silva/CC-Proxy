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

/**
 *
 * @author To_si
 */


public class ClientMonitorUDP {
    private static String ipName;
    private static long pingFreq;
    
    
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
        UserInput input= new UserInput();
        
        DatagramSocket watcherSocket= new DatagramSocket(5555);
        DatagramSocket clientSocket = new DatagramSocket();
        
        getInputs();
        InetAddress IPAddress = InetAddress.getByName(ipName);
        InetAddress host = InetAddress.getLocalHost(); 
        String IPad=host.toString().replaceAll(".*/", "");
        System.out.println(host);
        byte[] ip= IPad.getBytes();
        
        long startTime = System.nanoTime();
        Thread t = new Thread(new Benchmark()); 
        t.start();
        t.join();
        int benchmarkScore = (int)(100/((System.nanoTime() - startTime)*10E-10));
        
        System.out.println("Score: "+benchmarkScore);
        
        PacketInfo pi=new PacketInfo();
        
        System.out.println("\n\tType 'quit' to exit the program.\n");
        Thread watcher = new Thread(new Watcher(pi,input,watcherSocket));
        watcher.start();
        Thread inputScanner= new Thread(new InputScanner(input));
        inputScanner.start();
        
        while (!input.getQuit()) {
            synchronized(pi){
                i=2;
               
                if (pi.getSN()==100) {pi.setSN(1);}

                if (pi.getPoll()){
                    byte[] sendData = new byte[ip.length+2];
                    OperatingSystemMXBean osBean=(OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
                    cpuLD=((osBean.getSystemCpuLoad())*100);
                    
                    sendData[0]=1;
                    sendData[1]=(byte)cpuLD;
                    for(byte b : ip){
                        sendData[i]=b;
                        i++;
                    }
                    DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,IPAddress,5555);             
                    clientSocket.send(sendPacket);       
                    pi.setPoll(false);
                }else{
                    if(pi.getSN()==0) {
                        byte[] sendData = new byte[ip.length+3];
                        System.out.println("Sequence number: "+pi.getSN());
                        sendData[0]= 0;
                        sendData[1]=(byte) 0;
                        for(byte b : ip){
                            sendData[i]=b;
                            i++;
                        }                    
                        sendData[i]=(byte)benchmarkScore;    
                        DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,IPAddress,5555);             
                        clientSocket.send(sendPacket);               
                    }else{
                        byte[] sendData = new byte[ip.length+2];
                        System.out.println("Sequence number: "+pi.getSN());
                        sendData[0]= 0;
                        sendData[1]=(byte) pi.getSN();
                        for(byte b : ip){
                            sendData[i]=b;
                            i++;
                        }        
                        DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,IPAddress,5555);             
                        clientSocket.send(sendPacket); 
                        pi.setSN(pi.getSN()+1);                          
                    }
                }

                if (!pi.getPoll()) pi.wait(pingFreq);
            }
        }
        watcherSocket.close();
        watcher.join();
        inputScanner.join();
    }    
}