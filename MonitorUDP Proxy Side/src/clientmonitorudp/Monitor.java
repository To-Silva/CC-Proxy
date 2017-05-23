/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitorudp;

import java.io.IOException;
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
public class Monitor implements Runnable {
    private ArrayBlockingQueue<byte[]> packets;
    private ServerStatus server;
    private ConcurrentSkipListSet<ServerStatus> table;
    private InetAddress ClIP;
    private int prevSeqNum;
    private HashMap<InetAddress,ArrayBlockingQueue> queues;
    private UserInput ui;
    
    private int timeout,timeRem;
    
    public Monitor (ConcurrentSkipListSet<ServerStatus> t,ArrayBlockingQueue p,ServerStatus s,InetAddress ClIP,HashMap h,UserInput u,int timeout,int timeRem){
        this.server=s;
        this.packets=p;
        this.table=t;
        this.ClIP=ClIP;
        this.queues=h;
        this.ui=u;
        this.timeout=timeout;
        this.timeRem=timeRem;
        prevSeqNum=0;
    }
    
    @Override
    public void run(){
        boolean timedOut=false,active=true;
        int seqNum = 0;
        
        
        while(active&&!ui.getQuit()){
            byte[] receiveData=null;
            boolean correctPacket=false;
            try {
                if (!timedOut){
                    while(!correctPacket){
                        receiveData = packets.poll(timeout,TimeUnit.SECONDS);
                        correctPacket=true;
                        if (receiveData!=null&&receiveData[0]==1) {correctPacket=false;prevSeqNum=++seqNum;}
                    }
                }else{
                    receiveData = packets.poll(timeRem,TimeUnit.SECONDS);
                    if(receiveData!=null){
                        timedOut=false;
                    }else{
                        table.remove(server);
                        queues.remove(ClIP);
                        active=false;
                        System.out.println("Server with IP "+server.getIP()+" got removed for inactivity.");
                    }
                }
                if(active&&!ui.getQuit()){
                    if(receiveData!=null){
                        seqNum=receiveData[1];
                        if(seqNum!=0){
                            if ((Math.abs(seqNum-prevSeqNum)>1||seqNum==prevSeqNum)){
                                if (seqNum>prevSeqNum){
                                    synchronized(table){
                                        table.remove(server);
                                        server.updatePL(seqNum-prevSeqNum-1);
                                        table.add(server);
                                    }
                                }else{
                                    synchronized(table){
                                        table.remove(server);
                                        server.updatePL((100-prevSeqNum)+seqNum-1);
                                        table.add(server);
                                    }
                                }
                            }
                            prevSeqNum=seqNum;
                        }else{
                            prevSeqNum=0;
                            synchronized(table){
                                table.remove(server);
                                server.resetPL();
                                table.add(server);
                            }
                        }                            
                    }else{
                        synchronized(table){
                            table.remove(server);
                            server.setValid(0);
                            table.add(server);
                        }
                        System.out.println("Server with IP "+server.getIP()+" timed out.");
                        timedOut=true;
                    }
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Monitor.class.getName()).log(Level.SEVERE, null, ex);
            }                
        }
        if (table.contains(server))table.remove(server);
    }
}
