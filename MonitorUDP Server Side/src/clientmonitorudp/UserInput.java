/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitorudp;

/*
    Classe que guarda um boleano que informa as threads se o utilizador escreveu quit no terminal.
 */
class UserInput {
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
