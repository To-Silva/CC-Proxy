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
    
    public Monitor (ConcurrentSkipListSet<ServerStatus> t,ArrayBlockingQueue p,ServerStatus s,InetAddress ClIP,HashMap h){
        this.server=s;
        this.packets=p;
        this.table=t;
        this.ClIP=ClIP;
        this.queues=h;
        prevSeqNum=0;
    }
    
    @Override
    public void run(){
        boolean timedOut=false,active=true;
        int seqNum;
        
        
        while(active){
            byte[] receiveData;
            try {
                if (!timedOut){
                    receiveData = packets.poll(2,TimeUnit.SECONDS);
                }else{
                    receiveData = packets.poll(10,TimeUnit.SECONDS);
                    if(receiveData!=null){
                        timedOut=false;
                        server.setValid(1);
                    }else{
                        table.remove(server);
                        queues.remove(ClIP);
                        active=false;
                        System.out.println("Server with IP "+server.getIP()+" got removed for inactivity.");
                    }
                }
                if(active){
                    if(receiveData!=null){
                        seqNum=receiveData[1] & 0xFF;
                        if(seqNum!=0){
                            if (server.getValid()==0)server.setValid(1);
                            if ((Math.abs(seqNum-prevSeqNum)>1||seqNum==prevSeqNum)&&seqNum!=0){
                                if (seqNum>prevSeqNum){
                                    server.updatePL(seqNum-prevSeqNum-1);
                                }else{
                                    server.updatePL((100-prevSeqNum)+seqNum-1);
                                }
                            }
                            prevSeqNum=seqNum;
                        }else{
                                prevSeqNum=0;
                                server.updatePL(0);
                        }                            
                    }else{
                        server.setValid(0);
                        System.out.println("Server with IP "+server.getIP()+" timed out.");
                        timedOut=true;
                    }
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Monitor.class.getName()).log(Level.SEVERE, null, ex);
            }                
        }
    }
}
