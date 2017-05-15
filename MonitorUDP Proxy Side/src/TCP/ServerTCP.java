/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TCP;

/**
 *
 * @author jpedr
 */
    import java.io.*;
    import java.net.*;
    
public class ServerTCP {
    
    public static void main(String argv[]) throws Exception {
        String clientSentence;
        String capitalizedSentence;
        ServerSocket ss = new ServerSocket(1234);

        while (true) {
            Socket cs = ss.accept();
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(cs.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(cs.getOutputStream());
            clientSentence = inFromClient.readLine();
            System.out.println("Received: " + clientSentence);
            capitalizedSentence = clientSentence.toUpperCase() + '\n';
            outToClient.writeBytes(capitalizedSentence);
        }
    }
}
