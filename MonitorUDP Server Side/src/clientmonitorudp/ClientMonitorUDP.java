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
        InetAddress IPAddress = InetAddress.getByName("192.168.1.4");
        InetAddress host = InetAddress.getLocalHost(); 
        String IPad=host.toString().replaceAll(".*/", "");
        System.out.println(IPad);
        byte[] ip= IPad.getBytes();
        byte[] sendData = new byte[100];
        
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
            i=2;
            sendData[0]=(byte)pi.getSN();
            if (pi.getSN()==100) {pi.setSN(1);}
            
            if (pi.getSN()!=0){
                OperatingSystemMXBean osBean=(OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
                cpuLD=((osBean.getSystemCpuLoad())*100);
                cpuLoad=(int)cpuLD;
                sendData[1]=(byte)cpuLD;
            }else{
                sendData[1]=(byte)benchmarkScore;
            }
            for(byte b : ip){
                sendData[i]=b;
                i++;
            }
            
            
            DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,IPAddress,5555);             
            clientSocket.send(sendPacket);
            pi.setSN(pi.getSN()+1);
            sleep(1000);
        }
    }    
}