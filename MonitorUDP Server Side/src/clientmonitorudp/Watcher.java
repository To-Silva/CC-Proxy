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
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author To_si
 */
public class Watcher implements Runnable {
    
    private final PacketInfo pi;
    private UserInput ui;
    private DatagramSocket serverSocket;
    
    public Watcher(PacketInfo pi,UserInput u,DatagramSocket s){
        this.pi=pi;
        this.serverSocket=s;
        this.ui=u;
    }
    
    @Override
    public void run() {
        boolean initialized=false;
        try{
            while(!ui.getQuit()){

                byte[] receiveData= new byte[2];
                DatagramPacket receivePacket= new DatagramPacket(receiveData,receiveData.length);
                serverSocket.receive(receivePacket);

                receiveData = receivePacket.getData();
                int i2 = receiveData[0] & 0xFF;
                if (i2==0){
                    System.out.println("received poll");
                    synchronized(pi){
                        if (!initialized){
                            pi.setSN(1);
                            initialized=true;
                        }else{
                            pi.setPoll(true);
                            pi.notify(); 
                        }
                    }
                }
            }
        } catch (SocketException  | UnknownHostException ex) {
            
        } catch (IOException ex) {
            Logger.getLogger(Watcher.class.getName()).log(Level.SEVERE, null, ex);
        }      
    }
}
