/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitorudp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author To_si
 */
class TCPConnection implements Runnable {
    private final ConcurrentSkipListSet<ServerStatus> table;
    private Socket clientSocket;
    
    TCPConnection(ConcurrentSkipListSet<ServerStatus> table, Socket clientSocket) {
        this.table=table;
        this.clientSocket=clientSocket;
    }

    @Override
    public void run() {
        int valid=0;
        ServerStatus backend = null;
        BufferedReader inFromClient;
        try {
            inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream());
            System.out.println("Received Request");
            synchronized(table){
                while(table.isEmpty()||valid==0){
                    table.wait();
                    backend=table.first();
                    valid=backend.getValid();
                }            
                table.remove(backend);
                backend.incrementRN();
                table.add(backend);
            }
            Socket backendSocket=new Socket(backend.getIP(),80);

            BufferedReader inFromBackend = new BufferedReader(new InputStreamReader(backendSocket.getInputStream()));
            DataOutputStream outToBackend = new DataOutputStream(backendSocket.getOutputStream());

            outToBackend.write(inFromClient.read());

            outToClient.write(inFromBackend.read());
            if (table.contains(backend)){
                synchronized(table){
                    table.remove(backend);
                    backend.decreaseRN();
                    table.add(backend);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(TCPConnection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(TCPConnection.class.getName()).log(Level.SEVERE, null, ex);
        }            
    }
    
}
