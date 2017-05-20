/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitorudp;

import java.util.Scanner;

/**
 *
 * @author To_si
 */
public class InputScanner implements Runnable {

    private UserInput ui;
    
    public InputScanner(UserInput u) {
        this.ui=u;
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
    }
    
    
}
