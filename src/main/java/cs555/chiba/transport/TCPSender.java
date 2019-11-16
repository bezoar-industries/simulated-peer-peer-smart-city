/**
 * The TCPSender holds a TCP socket and sends messages
 * held in a LinkedBlockingQueue.
 *
 * @author Kevin Bruhwiler
 * @version 1.0
 * @since 2019-06-11
 */
package cs555.chiba.transport;

import cs555.chiba.service.Identity;
import cs555.chiba.util.Utilities;

import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TCPSender implements Runnable, AutoCloseable {

   private static final Logger logger = Logger.getLogger(TCPSender.class.getName());

   private Socket socket;
   private DataOutputStream s_out;
   private LinkedBlockingQueue<byte[]> messageQueue;
   private Identity identity;
   private boolean dead = false;

   public TCPSender(Socket socket) throws IOException {
      this.socket = socket;
      this.s_out = new DataOutputStream(socket.getOutputStream());
      this.messageQueue = new LinkedBlockingQueue<>();
      this.identity = Identity.builder().withSocketAddress(socket.getRemoteSocketAddress()).build();
   }

   /**
    * Add a message to the queue of messages waiting to be sent
    * @param message The serialized message
    */
   public void addMessage(byte[] message) {
      messageQueue.add(message);
   }

   /**
    * Write a serialized message to the socket
    * @param message The serialized message
    */
   public void sendMessage(byte[] message) {
      int size = message.length;
      synchronized (s_out) {
         try {
            s_out.writeInt(size); //start with size of message
            s_out.write(message, 0, size);
            s_out.flush();
         }
         catch (EOFException e) {
            logger.severe("The Tcp Connection output stream on [" + this.identity.getIdentityKey() + "] has been closed remotely");
            this.dead = true;
         }
         catch (IOException e) {
            logger.log(Level.SEVERE, "TCPSender.sendMessage() ", e);
         }
      }
   }

   public void run() {
      while (!Thread.currentThread().isInterrupted() && !this.dead) {
         byte[] nextMessage;
         try {
            //Block until there are messages in the queue
            nextMessage = messageQueue.take();
            sendMessage(nextMessage);
         }
         catch (Exception e) {
            if (!this.dead) {
               logger.log(Level.SEVERE, "TCPSender error:", e);
            }
            break;
         }
      }
      close();
      logger.info("TCPSender Closed [" + this.identity.getIdentityKey() + "]");
   }

   /**
    * Close the socket
    */
   @Override public void close() {
      this.dead = true;
      Utilities.closeQuietly(socket);
   }

   public Socket getSocket() {
      return socket;
   }
}
