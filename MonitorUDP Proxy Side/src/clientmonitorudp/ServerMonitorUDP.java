/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import clientmonitorudp.Monitor;
import clientmonitorudp.ServerStatus;
import clientmonitorudp.serverComparator;
import java.io.IOException;
import static java.lang.Thread.sleep;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 *
 * @author To_si
 */
public class ServerMonitorUDP {

    /**
     * @param args the command line arguments
     */
    
    public static void main(String[] args) throws SocketException, IOException, InterruptedException {
        ArrayList ips= new ArrayList();
        ConcurrentSkipListSet<ServerStatus> table = new ConcurrentSkipListSet<ServerStatus>(new serverComparator());
        DatagramSocket serverSocket= new DatagramSocket(5555);
        byte[] receiveData= new byte[100];
        InetAddress Address = InetAddress.getLocalHost(); 
        System.out.println(Address);
        while(true){
            
            DatagramPacket receivePacket= new DatagramPacket(receiveData,receiveData.length);
            serverSocket.receive(receivePacket);
            InetAddress ClIP= receivePacket.getAddress();
            if (!ips.contains(ClIP)){
                ips.add(ClIP);
                //String receivedString=new String(receivePacket.getData());
                receiveData = receivePacket.getData();
                int i2 = receiveData[0] & 0xFF;
                ServerStatus stat= new ServerStatus(i2,0,50,ClIP);
                Thread t = new Thread(new Monitor(stat,table,serverSocket,ClIP));
                t.start();
            }
            
            //for debugging 
            Iterator it=table.iterator();
            while(it.hasNext()){
                ServerStatus s=(ServerStatus) it.next();
                System.out.println("host: "+s.getIP()+"\n\t-CPULoad:"+s.getCPULoad());
            }
            //for debugging
            
            sleep(1000);
        }
    }
}
