/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitorudp;

    import java.io.*;
    import java.net.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TCPlayer implements Runnable {
    private ConcurrentSkipListSet<ServerStatus> table;

    public TCPlayer(ConcurrentSkipListSet<ServerStatus> table) {
        this.table=table;
    }
    
    
    @Override
    public void run(){
        ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        ServerStatus backend;
        ServerSocket inputSocket;
        try {
            inputSocket = new ServerSocket(80);
            while (true) {
                Socket clientSocket = inputSocket.accept();
                TCPConnection connection= new TCPConnection(table,clientSocket,inputSocket);
                threadPool.execute(connection);
        
            }
        } catch (IOException ex) {
            Logger.getLogger(TCPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
}
