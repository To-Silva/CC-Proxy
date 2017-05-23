/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitorudp;

import java.io.IOException;
import static java.lang.Thread.sleep;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author To_si
 */
public class StatusManager implements Runnable {
    private DatagramSocket serverSocket;
    private ArrayBlockingQueue<byte[]> packetsType1;
    private ServerStatus server;
    private ConcurrentSkipListSet<ServerStatus> table;
    private InetAddress ClIP;
    private HashMap<InetAddress,ArrayBlockingQueue> queues;
    private UserInput ui;
    
    private long pollFreq;
    private int timeout;
    
    public StatusManager (ConcurrentSkipListSet<ServerStatus> t,ArrayBlockingQueue p,ServerStatus s,InetAddress ClIP,DatagramSocket socket,HashMap h,UserInput u,long pollFreq,int timeout){
        this.serverSocket=socket;
        this.server=s;
        this.packetsType1=p;
        this.table=t;
        this.ClIP=ClIP;
        this.queues=h;
        this.ui=u;
        this.pollFreq=pollFreq;
        this.timeout=timeout;
    }
    
    @Override
    public void run(){
        long startTime,RTT;
        boolean active=true;
        int cpuL;
        
        
        byte[] sendData = new byte[1];
        sendData[0]=(byte)0;
        DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,ClIP,5555);  
        try {
            serverSocket.send(sendPacket);
        } catch (IOException ex) {
            Logger.getLogger(StatusManager.class.getName()).log(Level.SEVERE, null, ex);
        }              
        
        while(active&&!ui.getQuit()){
            byte[] receiveData;
            try {
                if (table.contains(server)){
                    startTime = System.nanoTime(); 
                    try {
                        serverSocket.send(sendPacket);
                    } catch (IOException ex) {
                        Logger.getLogger(StatusManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    System.out.println("Sent poll to "+ClIP);          
                
                    receiveData = packetsType1.poll(timeout,TimeUnit.SECONDS);
                    if (table.contains(server)&&!ui.getQuit()){
                        if (receiveData!=null) {
                            RTT=System.nanoTime() - startTime;
                            cpuL = receiveData[1] & 0xFF;                            
                            synchronized (table){
                                table.remove(server);
                                server.updateRTT((float) (RTT/1e6));
                                server.updatecpuLoad(cpuL);
                                if (server.getValid()==0){
                                    server.setValid(1);
                                    table.add(server); 
                                    table.notify();
                                }else{
                                    table.add(server); 
                                }
                            }
                        }else{
                            synchronized (table){
                                table.remove(server);
                                server.setValid(0);
                                server.updatePL(1);
                                System.out.println("Server with IP "+server.getIP()+" did not respond to poll.");
                                table.add(server);
                            }
                        }
                    }
                }
                if (!table.contains(server))active=false;
                sleep(pollFreq);
            } catch (InterruptedException ex) {
                Logger.getLogger(Monitor.class.getName()).log(Level.SEVERE, null, ex);
            }                
        }
        if (table.contains(server))table.remove(server);
        queues.remove(ClIP);
    }
}
