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
    private int prevSeqNum;
    
    public StatusManager (ConcurrentSkipListSet<ServerStatus> t,ArrayBlockingQueue p,ServerStatus s,InetAddress ClIP,DatagramSocket socket){
        this.serverSocket=socket;
        this.server=s;
        this.packets=p;
        this.table=t;
        this.ClIP=ClIP;
        prevSeqNum=0;
    }
    
    @Override
    public void run(){
        boolean timedOut=false,active=true;
        int type,seqNum,cpuL;
        long startTime = System.currentTimeMillis();
        byte[] ipBytes;
        String ip;
        
        table.add(server);
        byte[] sendData = new byte[1];
        sendData[0]=(byte)0;
        DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,ClIP,5555);  
        
        while(active){
            byte[] receiveData;
            try {
                
                try {
                    serverSocket.send(sendPacket);
                } catch (IOException ex) {
                    Logger.getLogger(StatusManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println("Sent poll to "+ClIP);        
                
                receiveData = packets.poll(2,TimeUnit.SECONDS);
                if(receiveData!=null){
                    cpuL = receiveData[1] & 0xFF;
                    table.remove(server);
                    server.updatecpuLoad(cpuL);
                    table.add(server);                          
                }else{
                    server.setValid(0);
                    System.out.println("Server with IP "+server.getIP()+" did not respond to poll.");
                    timedOut=true;
                }
                sleep(3000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Monitor.class.getName()).log(Level.SEVERE, null, ex);
            }                
        }
    }
}
