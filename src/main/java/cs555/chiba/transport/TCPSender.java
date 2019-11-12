/**
* The TCPSender holds a TCP socket and sends messages
* held in a LinkedBlockingQueue.
*
* @author  Kevin Bruhwiler
* @version 1.0
* @since   2019-06-11 
*/
package cs555.chiba.transport;

import java.io.*;
import java.net.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TCPSender implements Runnable, AutoCloseable {
	private static final Logger logger = Logger.getLogger(TCPSender.class.getName());

	private Socket socket;
    private DataOutputStream s_out;
    private LinkedBlockingQueue<byte[]> messageQueue;


    public TCPSender(Socket socket) throws IOException{
        this.socket = socket;
        this.s_out = new DataOutputStream(socket.getOutputStream());
        this.messageQueue = new LinkedBlockingQueue<byte[]>();
    }

    /**
     * Add a message to the queue of messages waiting to be sent
     * @param message The serialized message
     */
    public void addMessage(byte[] message){
    	messageQueue.add(message);
    }
    
    /**
     * Write a serialized message to the socket
     * @param message The serialized message
     */
    public void sendMessage(byte[] message) {
    	int size = message.length;
    	synchronized(s_out) {
	        try {
	            s_out.writeInt(size); //start with size of message
	            s_out.write(message, 0, size);
	            s_out.flush();
	        } catch (IOException e) {
				  logger.log(Level.SEVERE, "TCPSender.sendMessage() ", e);
	        }
    	}
    }

    public void run(){
        while (!Thread.currentThread().isInterrupted() && socket != null) {
        	byte[] nextMessage;
			try {
				//Block until there are messages in the queue
				nextMessage = messageQueue.take();
				if (nextMessage != null) { 
	                sendMessage(nextMessage);
	            }
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE, "TCPSender.run(): ", e);
				close();
			}
        }
    }
    
    /**
     * Close the socket
     */
    @Override
    public void close() {
    	try {
			this.socket.close();
			this.socket = null;
		} catch (IOException e) {
			 logger.log(Level.SEVERE, "TCPSender.close() ", e);
		}
    }

    public Socket getSocket() {
        return socket;
    }
}
