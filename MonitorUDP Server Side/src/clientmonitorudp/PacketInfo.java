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
public class PacketInfo {
    private int seqNum;
    private boolean poll;
    public PacketInfo(){
        seqNum=0;
        poll=false;
    }
    
    public int getSN(){
        return this.seqNum;
    }
    
     public boolean getPoll(){
        return this.poll;
    }
  
    public void setSN(int sn){
        this.seqNum=sn;
    }

    public void setPoll(boolean p){
        this.poll=p;
    }    
    
}
