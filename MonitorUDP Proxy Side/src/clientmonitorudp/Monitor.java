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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;
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
    
    public Monitor (ConcurrentSkipListSet<ServerStatus> t,ArrayBlockingQueue p,ServerStatus s,InetAddress ClIP){
        this.server=s;
        this.packets=p;
        this.table=t;
        this.ClIP=ClIP;
        prevSeqNum=0;
    }
    
    @Override
    public void run(){
        int seqNum,cpuL;
        long startTime = System.currentTimeMillis();
        byte[] ipBytes;
        String ip;
        
        table.add(server);
        while(true){
            byte[] receiveData;
            try {
                receiveData = packets.take();
                ipBytes=Arrays.copyOfRange(receiveData,2,receiveData.length);
                ip=new String(ipBytes);
                String IPad=server.getIP().toString().replaceAll(".*/", "").trim();
                if (IPad.equals(ip.trim())){
                    seqNum=receiveData[0] & 0xFF;
                    cpuL = receiveData[1] & 0xFF;
                    table.remove(server);
                    System.out.println("prev: "+prevSeqNum+"\n seq: "+seqNum);
                    if ((Math.abs(seqNum-prevSeqNum)>1||seqNum==prevSeqNum)&&seqNum!=0){
                        if (seqNum>prevSeqNum){
                            server.updatePL(seqNum-prevSeqNum-1);
                        }else{
                            server.updatePL((100-prevSeqNum)+seqNum-1);
                        }
                    }
                    prevSeqNum=seqNum;
                    server.updatecpuLoad(cpuL);
                    table.add(server);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Monitor.class.getName()).log(Level.SEVERE, null, ex);
            }                
        }
    }
}
