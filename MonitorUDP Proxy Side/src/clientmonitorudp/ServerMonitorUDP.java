/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import clientmonitorudp.Monitor;
import clientmonitorudp.ServerStatus;
import clientmonitorudp.serverComparator;
import java.io.IOException;
import static java.lang.Thread.sleep;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 *
 * @author To_si
 */
public class ServerMonitorUDP {

    /**
     * @param args the command line arguments
     */
    
    public static void main(String[] args) throws SocketException, IOException, InterruptedException {
        int tipo, benchCPU,seqNumb;
        byte[] ipBytes;
        String ip;
        
        ArrayList ips= new ArrayList();
        ConcurrentSkipListSet<ServerStatus> table = new ConcurrentSkipListSet<ServerStatus>(new serverComparator());
        DatagramSocket serverSocket= new DatagramSocket(5555);
        byte[] receiveData= new byte[100];
        InetAddress Address = InetAddress.getLocalHost(); 
        System.out.println(Address);
        while(true){
            
            DatagramPacket receivePacket= new DatagramPacket(receiveData,receiveData.length);
            serverSocket.receive(receivePacket);
            receiveData = receivePacket.getData();
            seqNumb=receiveData[0] & 0xFF;
            ipBytes=Arrays.copyOfRange(receiveData,2,receiveData.length);
            ip=new String(ipBytes);
            System.out.println(ip);
            InetAddress ClIP = InetAddress.getByName(ip);
            
            
            if (!ips.contains(ClIP)){
                if(seqNumb==0){
                    ips.add(ClIP);
                    //String receivedString=new String(receivePacket.getData());
                    benchCPU = receiveData[1] & 0xFF;
                    ServerStatus stat= new ServerStatus(benchCPU,ClIP);
                    Thread t = new Thread(new Monitor(stat,table,serverSocket,ClIP));
                    t.start();
                }
                byte[] sendData = new byte[2];
                sendData[0]=(byte)0;
                DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,ClIP,5555);  
                serverSocket.send(sendPacket);
                System.out.println("Permission granted to "+ClIP);
            }
            
            //for debugging 
            Iterator it=table.iterator();
            while(it.hasNext()){
                ServerStatus s=(ServerStatus) it.next();
                System.out.println("host: "+(s.getIP()).toString().replaceAll(".*/", "")+"\n\t-CPU Score:"+s.getBenchmark()+"\t-CPU Load:"+s.getCPULoad()+"\n\t-Number of packets lost:"+s.getPacketsLost());
            }
            //for debugging
            
        }
    }
}
