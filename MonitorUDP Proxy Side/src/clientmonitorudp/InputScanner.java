/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitorudp;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.Scanner;

/**
 *
 * @author To_si
 */
public class InputScanner implements Runnable {

    private UserInput ui;
    private DatagramSocket mainSocket;
    
    public InputScanner(UserInput u,DatagramSocket s) {
        this.ui=u;
        this.mainSocket=s;
    }

    @Override
    public void run() {
        String input;
        boolean quit=false;
        Scanner in = new Scanner (System.in);
        while(!quit){        
            input=in.nextLine();
            if (input.equals("quit"))quit=true;
        }
        ui.setQuit();
        mainSocket.close();
    }
    
    
}
