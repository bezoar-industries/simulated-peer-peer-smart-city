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
import java.util.logging.Level;
import java.util.logging.Logger;

import cs555.chiba.wireformats.EventFactory;


public class TCPReceiverThread implements Runnable, AutoCloseable {
    private static final Logger logger = Logger.getLogger(TCPReceiverThread.class.getName());

    private Socket socket;
    private DataInputStream s_in;
    private EventFactory factory;


    public TCPReceiverThread(Socket socket, EventFactory factory) throws IOException{
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
                logger.log(Level.SEVERE, "TCPReceiverThread.run() ", e);
                break;
            }
        }
        close();
    }

    /**
     * Close the ReceiverSocket
     */
    @Override
    public void close() {
        try {
            socket.close();
            this.socket = null;
        } catch (IOException e){
            logger.log(Level.SEVERE, "Failed to Close", e);
        }
    }
}