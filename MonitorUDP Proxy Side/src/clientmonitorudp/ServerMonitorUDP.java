/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import clientmonitorudp.Monitor;
import clientmonitorudp.ServerStatus;
import clientmonitorudp.StatusManager;
import clientmonitorudp.serverComparator;
import java.io.IOException;
import static java.lang.Thread.sleep;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *
 * @author To_si
 */
public class ServerMonitorUDP {

    /**
     * @param args the command line arguments
     */
    
    public static void main(String[] args) throws SocketException, IOException, InterruptedException {
        int tipo, benchCPU,seqNumb,type;
        byte[] ipBytes;
        String ip;
        
        HashMap<InetAddress,ArrayBlockingQueue> packetType0Queues= new HashMap<InetAddress,ArrayBlockingQueue>();
        HashMap<InetAddress,ArrayBlockingQueue> packetType1Queues= new HashMap<InetAddress,ArrayBlockingQueue>();
        ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        
        ConcurrentSkipListSet<ServerStatus> table = new ConcurrentSkipListSet<ServerStatus>(new serverComparator());
        DatagramSocket serverSocket= new DatagramSocket(5555);
        byte[] receiveData= new byte[100];
        InetAddress Address = InetAddress.getLocalHost(); 
        System.out.println(Address);
        while(true){
            
            DatagramPacket receivePacket= new DatagramPacket(receiveData,receiveData.length);
            serverSocket.receive(receivePacket);
            receiveData = receivePacket.getData();
            type=receiveData[0] & 0xFF;
            
            
            if (type==0){
                seqNumb=receiveData[1] & 0xFF;
                if (seqNumb==0){
                    ipBytes=Arrays.copyOfRange(receiveData,2,receiveData.length-1);
                    ip=new String(ipBytes);
                    System.out.println(ip);
                    InetAddress ClIP = InetAddress.getByName(ip);
                    if(!packetType0Queues.containsKey(ClIP)){
                        
                        ArrayBlockingQueue<byte[]> packetsType0=new ArrayBlockingQueue<>(100);
                        ArrayBlockingQueue<byte[]> packetsType1=new ArrayBlockingQueue<>(100);
                        packetType0Queues.put(ClIP, packetsType0);
                        packetType1Queues.put(ClIP, packetsType1);
                        
                        benchCPU = receiveData[receiveData.length-1] & 0xFF;
                        ServerStatus stat= new ServerStatus(benchCPU,ClIP);
                        Monitor monitor=new Monitor(table,packetsType0,stat,ClIP);
                        StatusManager statusMan=new StatusManager(table,packetsType1,stat,ClIP,serverSocket);
                        threadPool.execute(monitor);
                        threadPool.execute(statusMan);

                        byte[] sendData = new byte[1];
                        sendData[0]=(byte)0;
                        DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,ClIP,5555);  
                        serverSocket.send(sendPacket);
                        System.out.println("Permission granted to "+ClIP);        
                    }
                }else{
                    ipBytes=Arrays.copyOfRange(receiveData,2,receiveData.length);
                    ip=new String(ipBytes);
                    System.out.println(ip);
                    InetAddress ClIP = InetAddress.getByName(ip);                        
                    packetType0Queues.get(ClIP).add(receiveData);    
                    
                    if (packetType0Queues.containsKey(ClIP))packetType0Queues.get(ClIP).add(receiveData);
                }
            }else{
                ipBytes=Arrays.copyOfRange(receiveData,2,receiveData.length);
                ip=new String(ipBytes);
                System.out.println(ip);
                InetAddress ClIP = InetAddress.getByName(ip);          
                
                if (packetType0Queues.containsKey(ClIP)) packetType1Queues.get(ClIP).add(receiveData);
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
