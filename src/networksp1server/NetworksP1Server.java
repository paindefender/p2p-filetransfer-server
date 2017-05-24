package networksp1server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import networksp1.FMTEntry;

/**
 *
 * @author Kanat Alimanov
 */

public class NetworksP1Server implements Runnable{
    Socket csocket;
    
    static volatile ArrayList<FMTEntry> LFMTEntries = new ArrayList<>();
    
    public NetworksP1Server(Socket csocket){
        this.csocket = csocket;
    }

    public static void main(String[] args) throws Exception{
        ServerSocket welcomeSocket = new ServerSocket(0);
        System.out.println("Listening on port " + welcomeSocket.getLocalPort());
        
        while(true){
            Socket connectionSocket = welcomeSocket.accept();
            new Thread(new NetworksP1Server(connectionSocket)).start();
        }
    }

    @Override
    public void run() {
        ArrayList<FMTEntry> thing = new ArrayList<>();
        try {
            System.out.println("thread separated");
            BufferedInputStream inFromClient = new BufferedInputStream(csocket.getInputStream());
            DataOutputStream outToClient = new DataOutputStream(csocket.getOutputStream());
            ObjectInputStream objectInput = new ObjectInputStream(csocket.getInputStream());
            ObjectOutputStream objectOutput = new ObjectOutputStream(csocket.getOutputStream());
            
            while (true){
                String s = "";
                for (int i=0; i<4; i++){ //Each instruction is 4 bytes long
                    int c = inFromClient.read();
                    s = s + (char)c;
                }
                System.out.println(s);
                if (s.equals("fmts")){
                    //System.out.println("getting fmts!");
                    thing = (ArrayList<FMTEntry>) objectInput.readObject();
                    for (FMTEntry entry: thing){
                        entry.ip = csocket.getLocalAddress().toString().substring(1);
                    }
                    
                    LFMTEntries.addAll(thing);
                } else if (s.equals("deld")){
                    LFMTEntries.removeAll(thing);
                    //System.out.println("Removed data");
                } else if (s.equals("gfmt")){
                    objectOutput.reset();
                    objectOutput.writeObject(LFMTEntries);
                    
                    for (FMTEntry f : LFMTEntries){
                        System.out.println(f);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e);
            LFMTEntries.removeAll(thing);
        }
    }
    
}
