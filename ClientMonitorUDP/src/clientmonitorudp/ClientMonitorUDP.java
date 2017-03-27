/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitorudp;

import java.io.IOException;
import static java.lang.Thread.sleep;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 *
 * @author To_si
 */
public class ClientMonitorUDP {
    public static void main(String[] args) throws SocketException, IOException, InterruptedException {
        double cpuLD;
        int cpuLoad;
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName("192.168.1.4");
        byte[] sendData = new byte[100];     
        while (true) {
            OperatingSystemMXBean osBean=(OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            cpuLD=((osBean.getSystemCpuLoad())*100);
            cpuLoad=(int)cpuLD;
            sendData[0]=(byte)cpuLoad;
            DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,IPAddress,5555);             
            clientSocket.send(sendPacket);
            sleep(1000);
        }
    }    
}
