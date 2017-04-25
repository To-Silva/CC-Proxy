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
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author To_si
 */
public class Monitor implements Runnable {
    private ServerStatus server;
    private ConcurrentSkipListSet<ServerStatus> table;
    private DatagramSocket serverSocket;
    private InetAddress ClIP;
    
    public Monitor (ServerStatus s,ConcurrentSkipListSet<ServerStatus> t,DatagramSocket d,InetAddress ClIP){
        this.server=s;
        this.table=t;
        this.serverSocket=d;
        this.ClIP=ClIP;
    }
    
    @Override
    public void run(){
        byte[] receiveData= new byte[100];
        table.add(server);
        while(true){
            DatagramPacket receivePacket= new DatagramPacket(receiveData,receiveData.length);
            try {            
                serverSocket.receive(receivePacket);
            } catch (IOException ex) {
                Logger.getLogger(Monitor.class.getName()).log(Level.SEVERE, null, ex);
            }
            InetAddress ip= receivePacket.getAddress();
            if (server.getIP().equals(ip)){
                int i2 = receiveData[0] & 0xFF;
                table.remove(server);
                server.updatecpuLoad(i2);
                System.out.println(ClIP + ": " +"CPU LOAD: "+ server.getCPULoad());
            }
        }
    }
}
