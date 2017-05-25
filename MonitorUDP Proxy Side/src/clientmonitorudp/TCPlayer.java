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
        //pool de threads para conecções com os clientes
        ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        try {
            while (!ui.getQuit()) {
                Socket clientSocket = inputSocket.accept();
                //recebida ligação, criar thread de conecção
                TCPConnection connection= new TCPConnection(table,clientSocket);
                threadPool.execute(connection);
        
            }
        } catch (IOException ex) {
            threadPool.shutdown();
        }        
        threadPool.shutdown();
    }
}
