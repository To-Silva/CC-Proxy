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

    private int valid;  //0 if outdated
    private int cpuLoad,requestNum,benchmarkScore;
    private InetAddress ip;
    
    public ServerStatus(int c,int r,int b,InetAddress i){
        cpuLoad=c;
        requestNum=r;
        benchmarkScore=b;
        ip=i;
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
}
