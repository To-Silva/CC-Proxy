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
    private UserInput ui;
    private ServerSocket inputSocket;

    public TCPlayer(ConcurrentSkipListSet<ServerStatus> table,UserInput u,ServerSocket s) {
        this.table=table;
        this.ui=u;
        this.inputSocket=s;
    }
    
    
    @Override
    public void run(){
        ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        try {
            while (!ui.getQuit()) {
                Socket clientSocket = inputSocket.accept();
                TCPConnection connection= new TCPConnection(table,clientSocket);
                threadPool.execute(connection);
        
            }
        } catch (IOException ex) {
        }        
        threadPool.shutdown();
    }
}
