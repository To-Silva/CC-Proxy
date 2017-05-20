/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitorudp;

/**
 *
 * @author To_si
 */
public class UserInput {
    private boolean quit;
    
    public UserInput(){
        this.quit=false;
    }
    
    public boolean getQuit(){
        return this.quit; 
    }
    
    public void setQuit(){
        this.quit=true; 
    }    
    
}
