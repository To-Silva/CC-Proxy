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
     * @throws java.net.SocketException
     * @throws java.lang.InterruptedException
     */
    
    
    public static void main(String[] args) throws SocketException, IOException, InterruptedException {
        int tipo, benchCPU,seqNumb,type;
        byte[] ipBytes;
        String ip;
        
        HashMap<InetAddress,ArrayBlockingQueue> packetType0Queues= new HashMap<>();
        HashMap<InetAddress,ArrayBlockingQueue> packetType1Queues= new HashMap<>();
        ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        
        ConcurrentSkipListSet<ServerStatus> table = new ConcurrentSkipListSet<>(new serverComparator());
        DatagramSocket serverSocket= new DatagramSocket(5555);
        byte[] receiveData= new byte[100];
        InetAddress Address = InetAddress.getLocalHost(); 
        System.out.println(Address);
        while(true){
            
            DatagramPacket receivePacket= new DatagramPacket(receiveData,receiveData.length);
            serverSocket.receive(receivePacket);
            receiveData = receivePacket.getData();
            type=receiveData[0] & 0xFF;
            
            if (receivePacket.getLength()>3){
                if (type==0){
                    seqNumb=receiveData[1] & 0xFF;
                    if (seqNumb==0){
                        ipBytes=Arrays.copyOfRange(receiveData,2,receivePacket.getLength()-1);
                        ip=new String(ipBytes);
                        InetAddress ClIP = InetAddress.getByName(ip);
                        if(!packetType0Queues.containsKey(ClIP)){
                            ArrayBlockingQueue<byte[]> packetsType0=new ArrayBlockingQueue<>(100);
                            ArrayBlockingQueue<byte[]> packetsType1=new ArrayBlockingQueue<>(100);
                            packetType0Queues.put(ClIP, packetsType0);
                            packetType1Queues.put(ClIP, packetsType1);

                            benchCPU = receiveData[receivePacket.getLength()-1] & 0xFF;
                            ServerStatus stat= new ServerStatus(benchCPU,ClIP);
                            table.add(stat);
                            Monitor monitor=new Monitor(table,packetsType0,stat,ClIP,packetType0Queues);
                            StatusManager statusMan=new StatusManager(table,packetsType1,stat,ClIP,serverSocket,packetType1Queues);
                            threadPool.execute(monitor);
                            threadPool.execute(statusMan);
                        }
                    }else{
                        ipBytes=Arrays.copyOfRange(receiveData,2,receivePacket.getLength());
                        ip=new String(ipBytes);
                        InetAddress ClIP = InetAddress.getByName(ip);     
                        if (packetType0Queues.containsKey(ClIP))packetType0Queues.get(ClIP).add(receiveData);
                    }
                }else{
                    ipBytes=Arrays.copyOfRange(receiveData,2,receivePacket.getLength());
                    ip=new String(ipBytes);
                    InetAddress ClIP = InetAddress.getByName(ip);          

                    if (packetType0Queues.containsKey(ClIP)) packetType1Queues.get(ClIP).add(receiveData);
                }
            }
            //for debugging 
            Iterator it=table.iterator();
            while(it.hasNext()){
                ServerStatus s=(ServerStatus) it.next();
                if (s.getValid()==1)System.out.println("host: "+(s.getIP()).toString().replaceAll(".*/", "")+"\n\t-CPU Score:"+s.getBenchmark()+"\t-CPU Load:"+s.getCPULoad()+"\t-RTT:"+s.getRTT()+"\n\t-Number of packets lost:"+s.getPacketsLost());
            }
            //for debugging            
        }
    }
}
