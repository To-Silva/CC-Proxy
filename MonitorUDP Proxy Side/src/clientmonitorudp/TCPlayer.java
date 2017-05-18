/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitorudp;

    import java.io.*;
    import java.net.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TCPlayer implements Runnable {
    private ConcurrentSkipListSet<ServerStatus> table;

    public TCPlayer(ConcurrentSkipListSet<ServerStatus> table) {
        this.table=table;
    }
    
    
    @Override
    public void run(){
        ServerStatus backend;
        ServerSocket inputSocket;
        try {
            inputSocket = new ServerSocket(80);
            while (true) {
                Socket clientSocket = inputSocket.accept();
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
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
            }
        } catch (IOException ex) {
            Logger.getLogger(TCPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
}
