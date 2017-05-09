/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitorudp;

import java.net.InetAddress;
import java.util.Comparator;
import org.apache.commons.lang3.builder.CompareToBuilder;

/**
 *
 * @author To_si
 */
public class ServerStatus {

    private int valid;  //0 if not initialized or outdated
    private int cpuLoad,requestNum,benchmarkScore,packetsLost;
    private float packetsLostTimeRatio;
    private InetAddress ip;
    
    public ServerStatus(int b,InetAddress i){
        benchmarkScore=b;
        requestNum=0;
        ip=i;
        packetsLost=0;
        packetsLostTimeRatio=0;
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
    
    public void updatePL(int pl){
        this.packetsLost+=pl;
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
}
