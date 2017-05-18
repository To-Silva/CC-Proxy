/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitorudp;

import java.io.IOException;
import static java.lang.Thread.sleep;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 *
 * @author To_si
 */
public class ClientMonitorUDP {
    public static void main(String[] args) throws SocketException, IOException, InterruptedException {
        double cpuLD;
        int cpuLoad,i;
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName("192.168.1.9");
        InetAddress host = InetAddress.getLocalHost(); 
        String IPad=host.toString().replaceAll(".*/", "");
        System.out.println(IPAddress);
        byte[] ip= IPad.getBytes();
        
        long startTime = System.nanoTime();
        Thread t = new Thread(new Benchmark()); 
        t.start();
        t.join();
        int benchmarkScore = (int)(100/((System.nanoTime() - startTime)*10E-10));
        
        System.out.println("Score: "+benchmarkScore);
        
        int ipSize= ip.length;     
        PacketInfo pi=new PacketInfo();
        
        Thread watcher = new Thread(new Watcher(pi));
        watcher.start();
        while (true) {
            synchronized(pi){
                i=2;
               
                if (pi.getSN()==100) {pi.setSN(0);}

                if (pi.getPoll()){
                    byte[] sendData = new byte[ip.length+2];
                    OperatingSystemMXBean osBean=(OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
                    cpuLD=((osBean.getSystemCpuLoad())*100);
                    cpuLoad=(int)cpuLD;
                    sendData[0]=(byte)1;
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
                        sendData[0]=(byte) 0;
                        sendData[1]=(byte) 0;
                        for(byte b : ip){
                            sendData[i]=b;
                            i++;
                        }                    
                        if (pi.getSN()==0) sendData[i]=(byte)benchmarkScore;    
                        System.out.println(i);
                        DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,IPAddress,5555);             
                        clientSocket.send(sendPacket); 
                        if (pi.getSN()!=0)pi.setSN(pi.getSN()+1);                          
                    }else{
                        byte[] sendData = new byte[ip.length+2];
                        System.out.println("Sequence number: "+pi.getSN());
                        sendData[0]=(byte) 0;
                        sendData[1]=(byte) pi.getSN();
                        for(byte b : ip){
                            sendData[i]=b;
                            i++;
                        }        
                        DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,IPAddress,5555);             
                        clientSocket.send(sendPacket); 
                        if (pi.getSN()!=0)pi.setSN(pi.getSN()+1);                          
                    }
                }

                if (!pi.getPoll()) pi.wait(1000);
            }
        }
    }    
}