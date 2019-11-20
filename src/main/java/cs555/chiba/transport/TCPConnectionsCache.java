/**
 * The TCPConnectionsCache stores references to all the sender/receiver threads.
 * It also contains utilities for sending messages.
 *
 * @author Kevin Bruhwiler
 * @version 1.0
 * @since 2019-06-11
 */

package cs555.chiba.transport;

import cs555.chiba.service.Identity;
import cs555.chiba.service.ServiceNode;
import cs555.chiba.util.Utilities;
import cs555.chiba.wireformats.EventFactory;
import cs555.chiba.wireformats.IntroductionMessage;

import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TCPConnectionsCache implements AutoCloseable {

   private static final Logger logger = Logger.getLogger(TCPConnectionsCache.class.getName());

   private ConcurrentHashMap<Identity, TCPReceiverThread> receiverThreads = new ConcurrentHashMap<>();
   private ConcurrentHashMap<Identity, TCPSender> senders = new ConcurrentHashMap<>();

   public TCPConnectionsCache() {
   }

   /**
    * Adds a receiver thread to the cache
    */
   void addReceiverThread(Identity ident, TCPReceiverThread receiver) {
      Thread receiverThread = new Thread(receiver);
      receiverThread.start();
      TCPReceiverThread old = this.receiverThreads.putIfAbsent(ident, receiver);

      if (old != null) {
         old.close();
      }
   }

   public void addConnection(Identity ident, EventFactory factory) {
      Socket sock = null;

      TCPSender node = this.senders.get(ident);

      if (node != null) {
         logger.info("Already connected to [" + ident.getIdentityKey() + "]");
         return;
      }

      try {
         sock = new Socket(ident.getHost(), ident.getPort());
         TCPSender sender = new TCPSender(sock);
         addSender(sender, ident);
         addReceiverThread(ident, new TCPReceiverThread(sock, factory));
         IntroductionMessage message = new IntroductionMessage(ServiceNode.getThisNode().getIdentity());
         sender.sendMessage(message.getBytes());
      }
      catch (IOException e) {
         logger.log(Level.SEVERE, "Failed to add connection to [" + ident.getIdentityKey() + "]", e);
         Utilities.closeQuietly(sock);
      }
   }

   /**
    * Adds a sender to the cache with the default ID
    * @param sender A reference to the sender object
    */
   public void addSender(TCPSender sender, Identity ident) {
      Thread senderThread = new Thread(sender);
      senderThread.start();
      TCPSender old = senders.putIfAbsent(ident, sender);

      if (old != null) {
         old.close();
      }
   }

   /**
    * Convenience to get a random sender from the cache
    * @returns TCPsender A random sender
    */
   public TCPSender getRandomSender() {
      Random generator = new Random();
      Object[] values = senders.values().toArray();
      return (TCPSender) values[generator.nextInt(values.length)];
   }

   /**
    * Removes a sender from the cache
    * @param ident The ID of the sender to be removed
    */
   public void removeSender(Identity ident) {
      senders.remove(ident);
   }

   /**
    * Check if we have a sender with the given ID
    * @returns boolean True if the sender exists
    */
   public boolean sendersContains(Identity ident) {
      return senders.containsKey(ident);
   }

   /**
    * Send a message to the sender with the given ID
    * @param ident The ID of the recipient
    * @param message The serialized message to be sent
    */
   public void send(Identity ident, byte[] message) {
      senders.get(ident).addMessage(message);
   }

   /**
    * Send a one off message to a peer.
    * This is builds and tears down a socket per message.
    * Do not use outside of registry communication.
    */
   public void sendSingle(Identity identity, byte[] message) {
      Socket sock = null;

      try {
         sock = new Socket(identity.getHost(), identity.getPort());
         TCPSender sender = new TCPSender(sock);
         sender.sendMessage(message);
         sender.close();
      }
      catch (Exception e) {
         logger.log(Level.SEVERE, "Failed to send message to [" + identity.getIdentityKey() + "]", e);
      }
      finally {
         Utilities.closeQuietly(sock);
      }
   }

   /**
    * Send a message a random sender
    * @param message The serialized message to be sent
    */
   public void sendToRandom(byte[] message) {
      getRandomSender().addMessage(message);
   }

   /**
    * Send a message to the sender with the given ID
    * @param exclude The ID of the excluded recipient
    * @param message The serialized message to be sent
    */
   public void sendToRandom(byte[] message, Identity exclude) {
      Random generator = new Random();
      Object[] keys = senders.keySet().toArray();

      Identity key;
      do {
         key = (Identity) keys[generator.nextInt(keys.length)];
      } while (!key.equals(exclude));

      TCPSender randomSender = senders.get(key);
      randomSender.addMessage(message);
   }

   /**
    * Convenience to send a message over every connection in the cache
    * @param message The serialized message to be sent
    */
   public void sendAll(byte[] message) {
      for (Identity key : senders.keySet()) {
         this.send(key, message); // note, the registry is not stored in the senders list
      }
   }

   /**
    * Convenience to send a message over every connection in the cache
    * except the registry
    * @param message The serialized message to be sent
    */
   public void sendAll(byte[] message, Identity exclude) {
      for (Identity key : senders.keySet()) {
         if (key != exclude)
            this.send(key, message); // note, the registry is not stored in the senders list
      }
   }

   public List<String> listConnections() {
      return Collections.list(this.receiverThreads.keys()).stream().map(Identity::getIdentityKey).collect(Collectors.toList());
   }

   @Override public void close() {
      this.receiverThreads.values().forEach(TCPReceiverThread::close);
      this.senders.values().forEach(TCPSender::close);
   }

   /**
    * When another node connects to this node, the remote host:port is not the server socket.  It's the generated socket.
    * In order to keep things sane, we're going to always identify connections by their server socket host:port.
    *
    * To do that, the node has to send their server host:port to us with an IntroductionMessage.  Then we have to correct
    * how we label the connected socket.
    */
   public void correctIdentity(Identity wrongIdent, Identity identity) {
      try {
         TCPReceiverThread receiver = this.receiverThreads.remove(wrongIdent);
         receiver.setIdentity(identity);
         this.receiverThreads.put(identity, receiver);
         TCPSender sender = new TCPSender(receiver.getSocket());
         addSender(sender, identity);
      }
      catch (IOException e) {
         logger.log(Level.SEVERE, "Failed to correct socket labels.  We may have duplicate connections. Wrong Label [" + wrongIdent.getIdentityKey() + "] Right Label [" + identity.getIdentityKey() + "]");
      }
   }

   public void removeConnection(Identity identity) {
      Utilities.closeQuietly(this.receiverThreads.remove(identity));
      Utilities.closeQuietly(this.senders.remove(identity));
   }
}
