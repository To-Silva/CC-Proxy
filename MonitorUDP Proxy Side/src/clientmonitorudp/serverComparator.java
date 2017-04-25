/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientmonitorudp;

import java.util.Comparator;
import org.apache.commons.lang3.builder.CompareToBuilder;

/**
 *
 * @author To_si
 */
public class serverComparator implements Comparator<ServerStatus> {
        Comparator<ServerStatus> speed = new Comparator<ServerStatus>() {

                @Override
                public int compare(ServerStatus s1, ServerStatus s2) {
                    float ratio;
                    if (s1.getBenchmark()>s2.getBenchmark()){
                        ratio=s1.getBenchmark()/s2.getBenchmark();
                        if(ratio>(s1.getRN()-s2.getRN())){
                            return 1;
                        }else{return -1;}
                    }else{
                        ratio=s2.getBenchmark()/s1.getBenchmark();
                        if(ratio>(s2.getRN()-s1.getRN())){
                            return -1;
                        }else{return 1;}                        
                    }        
                }
     };
    
        Comparator<ServerStatus> load = new Comparator<ServerStatus>() {

                @Override
                public int compare(ServerStatus s1, ServerStatus s2) {
                    if((s1.getCPULoad()>80&&s2.getCPULoad()>80)||(s1.getCPULoad()<80&&s2.getCPULoad()<80)) {
                        return 0;
                    }else{
                        if (s1.getCPULoad()>s2.getCPULoad()){
                            return -1;
                        }else{return 1;}
                    }
                }
     };
    
    @Override
    public int compare(ServerStatus s1,ServerStatus s2) {
        float ratio;
        
        return new CompareToBuilder()
                .append(s1.getValid(), s2.getValid())
                .append(s1,s2,load)
                .append(s1, s2,speed)
                .toComparison();    
    }
}
