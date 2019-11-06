/**
* The TCPRecieverThread reads messages coming over a TCP socket. 
* It expects the first thing in the message to be an integer message size.
*
* @author  Kevin Bruhwiler
* @version 1.0
* @since   2019-06-11 
*/
package cs555.chiba.transport;

import java.io.*;
import java.net.*;

import cs555.chiba.wireformats.EventFactory;


public class TCPRecieverThread implements Runnable{
    private Socket socket;
    private DataInputStream s_in;
    private EventFactory factory;

    public TCPRecieverThread(Socket socket, EventFactory factory) throws IOException{
        this.socket = socket;
        this.s_in = new DataInputStream(socket.getInputStream());
        this.factory = factory;
    }

    public void run(){
        int size;
        while (socket != null) {
            try {
            	//Read in message size
            	size = s_in.readInt();
                byte[] response = new byte[size];
                //Read size bytes from the sockets
                s_in.readFully(response, 0, size);
                //Send the serialized message to be processed
                factory.processMessage(response, socket);
            } catch (IOException e) {
                System.out.println("TCPRecieverThread.run() " + e);
                break;
            }
        }
    }
}