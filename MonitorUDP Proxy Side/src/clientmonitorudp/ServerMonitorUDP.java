/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import static java.lang.Thread.sleep;
import java.net.*;

/**
 *
 * @author To_si
 */
public class ServerMonitorUDP {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SocketException, IOException, InterruptedException {
        DatagramSocket serverSocket= new DatagramSocket(5555);
        byte[] receiveData= new byte[100];
        InetAddress Address = InetAddress.getLocalHost(); 
        System.out.println(Address);
        while(true){
            DatagramPacket receivePacket= new DatagramPacket(receiveData,receiveData.length);
            serverSocket.receive(receivePacket);
            InetAddress ClIP= receivePacket.getAddress();
            //String receivedString=new String(receivePacket.getData());
            receiveData = receivePacket.getData();
            int i2 = receiveData[0] & 0xFF;
            System.out.println(ClIP + ": " +"CPU LOAD: "+ i2);
            sleep(1000);
        }
    }
}
