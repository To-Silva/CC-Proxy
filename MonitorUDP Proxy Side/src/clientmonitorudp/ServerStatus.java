/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitorudp;

import java.net.InetAddress;
/*
    Guarda dados sobre o estado do servidor
*/
public class ServerStatus {

    private int valid;  //se não inicializado ou desatualizado (após timeout)
    private int cpuLoad,requestNum,benchmarkScore,packetsLost;
    private float RTT;
    private InetAddress ip;
    private long startTime; 
    
    public ServerStatus(int b,InetAddress i,long t){
        benchmarkScore=b;
        requestNum=0;
        ip=i;
        packetsLost=0;
        valid=0;
        this.startTime=t;
    }
    
    public void setValid(int v){
        valid=v;
    }
    
    public void setBenchmark(int b){
        benchmarkScore=b;
    }
    public void updateRTT(float r){
        this.RTT=r;
    }    
    public void updatecpuLoad(int c){
        this.cpuLoad=c;
    }
    public void incrementRN(){
        this.requestNum++;
    }
    public void decreaseRN(){
        this.requestNum--;
    }
    
    public float getPLRatio(){
        return (float) (this.packetsLost/(this.getTimeElapsed()*1e-9));
    }
    
    public void updatePL(int pl){
        this.packetsLost+=pl;
    }
    
    public void resetPL(){
        this.packetsLost=0;
    }
    
    public long getTimeElapsed(){
        return (System.nanoTime()-this.startTime);
    }
    
    public InetAddress getIP(){
        return this.ip;
    }
    
    public int getValid(){
        return this.valid;
    }
    public int getCPULoad(){
        return this.cpuLoad;
    }    
    public int getRN(){
        return this.requestNum;
    }    
    public int getBenchmark(){
        return this.benchmarkScore;
    }    
    public int getPacketsLost(){
        return this.packetsLost;
    }
    public float getRTT(){
        return this.RTT;
    }
}
