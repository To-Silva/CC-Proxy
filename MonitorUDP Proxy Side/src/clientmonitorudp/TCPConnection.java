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
    private ConcurrentSkipListSet<ServerStatus> table;
    private ServerSocket inputSocket;
    private Socket clientSocket;
    
    TCPConnection(ConcurrentSkipListSet<ServerStatus> table, Socket clientSocket, ServerSocket inputSocket) {
        this.table=table;
        this.clientSocket=clientSocket;
        this.inputSocket=inputSocket;
    }

    @Override
    public void run() {
        ServerStatus backend;
        BufferedReader inFromClient;
        try {
            inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream());
            System.out.println("Received Request");

            backend=table.first();
            table.remove(backend);
            backend.incrementRN();
            table.add(backend);
            Socket backendSocket=new Socket(backend.getIP(),80);

            BufferedReader inFromBackend = new BufferedReader(new InputStreamReader(backendSocket.getInputStream()));
            DataOutputStream outToBackend = new DataOutputStream(backendSocket.getOutputStream());

            outToBackend.write(inFromClient.read());

            outToClient.write(inFromBackend.read());
            table.remove(backend);
            backend.decreaseRN();
            table.add(backend);  
        } catch (IOException ex) {
            Logger.getLogger(TCPConnection.class.getName()).log(Level.SEVERE, null, ex);
        }            
    }
    
}
