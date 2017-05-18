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
    private ArrayBlockingQueue<byte[]> packets;
    private ServerStatus server;
    private ConcurrentSkipListSet<ServerStatus> table;
    private InetAddress ClIP;
    private HashMap<InetAddress,ArrayBlockingQueue> queues;
    
    public StatusManager (ConcurrentSkipListSet<ServerStatus> t,ArrayBlockingQueue p,ServerStatus s,InetAddress ClIP,DatagramSocket socket,HashMap h){
        this.serverSocket=socket;
        this.server=s;
        this.packets=p;
        this.table=t;
        this.ClIP=ClIP;
        this.queues=h;
    }
    
    @Override
    public void run(){
        long startTime,RTT;
        boolean active=true;
        int cpuL,pollFreq=3000;
        
        
        byte[] sendData = new byte[1];
        sendData[0]=(byte)0;
        DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,ClIP,5555);  
        try {
            serverSocket.send(sendPacket);
        } catch (IOException ex) {
            Logger.getLogger(StatusManager.class.getName()).log(Level.SEVERE, null, ex);
        }              
        
        while(active){
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
                
                    receiveData = packets.poll(2,TimeUnit.SECONDS);
                    if (receiveData!=null) {
                        RTT=System.nanoTime() - startTime;
                        table.remove(server);
                        server.updateRTT((float) (RTT/1e6));
                        table.add(server);                                
                    }                

                    if(receiveData!=null){
                        if (table.contains(server)){
                            cpuL = receiveData[1] & 0xFF;
                            table.remove(server);
                            server.updatecpuLoad(cpuL);
                            table.add(server);           
                        }
                    }else{
                        if (table.contains(server)){
                            table.remove(server);
                            server.setValid(0);
                            System.out.println("Server with IP "+server.getIP()+" did not respond to poll.");
                            table.add(server); 
                        }
                    }
                }
                if (!table.contains(server))active=false;
                sleep(pollFreq);
            } catch (InterruptedException ex) {
                Logger.getLogger(Monitor.class.getName()).log(Level.SEVERE, null, ex);
            }                
        }
        queues.remove(ClIP);
    }
}
