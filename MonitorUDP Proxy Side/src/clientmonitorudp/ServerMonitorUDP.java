/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import clientmonitorudp.InputScanner;
import clientmonitorudp.UserInput;
import clientmonitorudp.Monitor;
import clientmonitorudp.ServerStatus;
import clientmonitorudp.StatusManager;
import clientmonitorudp.TCPlayer;
import clientmonitorudp.serverComparator;
import java.io.IOException;
import static java.lang.Thread.sleep;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
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
    private static int timeout,timeUntilRemoved;
    private static long pollFreq;
    
    public static void getInputs(){
        boolean valid=false;
        Scanner in = new Scanner (System.in);
        while(!valid){        
            System.out.println("Insert timeout (s):");
            timeout=in.nextInt();
            valid=timeout>0;
            if (!valid) System.out.println("Invalid timeout.");
        }
        valid=false;
        while(!valid){        
            System.out.println("Insert time until server gets removed after timeout (s):");
            timeUntilRemoved=in.nextInt();
            valid=timeUntilRemoved>0;
            if (!valid) System.out.println("Invalid time.");
        }      
        valid=false;
        while(!valid){        
            System.out.println("Insert poll frequency (ms):");
            pollFreq=in.nextInt();
            valid=pollFreq>0;
            if (!valid) System.out.println("Invalid poll frequency.");
        }              
    }
        
    
    public static void main(String[] args) throws IOException, InterruptedException {
        int benchCPU,seqNumb,type;
        byte[] ipBytes;
        String ip;
        UserInput input= new UserInput();
        
        getInputs();
        HashMap<InetAddress,ArrayBlockingQueue> packetType0Queues= new HashMap<>();
        HashMap<InetAddress,ArrayBlockingQueue> packetType1Queues= new HashMap<>();
        ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        ConcurrentSkipListSet<ServerStatus> table = new ConcurrentSkipListSet<>(new serverComparator());
        ServerSocket tcpSocket=new ServerSocket(80);
        TCPlayer tcp= new TCPlayer(table,input,tcpSocket);
        threadPool.execute(tcp);        
        
        DatagramSocket serverSocket= new DatagramSocket(5555);
        byte[] receiveData= new byte[100];
        InetAddress Address = InetAddress.getLocalHost(); 
        System.out.println(Address);
        System.out.println("\n\tType 'quit' to exit the program.\n");
        InputScanner inputScanner=new InputScanner(input,serverSocket);
        threadPool.execute(inputScanner);             
        try{
            while(!input.getQuit()){
                DatagramPacket receivePacket= new DatagramPacket(receiveData,receiveData.length);       
                serverSocket.receive(receivePacket);
                receiveData = receivePacket.getData();
                type=receiveData[0];

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
                                ServerStatus stat= new ServerStatus(benchCPU,ClIP,System.nanoTime());
                                synchronized(table){
                                    table.add(stat);
                                    table.notify();
                                }
                                Monitor monitor=new Monitor(table,packetsType0,stat,ClIP,packetType0Queues,input,timeout,timeUntilRemoved);
                                StatusManager statusMan=new StatusManager(table,packetsType1,stat,ClIP,serverSocket,packetType1Queues,input,pollFreq,timeout);
                                threadPool.execute(monitor);
                                threadPool.execute(statusMan);
                            }else{
                                packetType0Queues.get(ClIP).add(receiveData);                                
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
                if (!table.isEmpty()){
                    ServerStatus s=table.first();
                    System.out.println("\nbest server: "+(s.getIP()).toString().replaceAll(".*/", "")+"\n\t-CPU Score:"+s.getBenchmark()+"\t-CPU Load:"+s.getCPULoad()+"\t-RTT:"+s.getRTT()+"\n\t-Number of packets lost:"+s.getPacketsLost()+"\n\t-Packets lost/time elapsed:"+s.getPLRatio()+" valid: "+s.getValid()+"\n");
                }
                //for debugging            
            }
        }catch(SocketException e){
            tcpSocket.close();
            threadPool.shutdown();        
        }
    }
}
