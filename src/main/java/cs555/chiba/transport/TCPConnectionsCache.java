/**
* The TCPConnectionsCache stores references to all the sender/receiver threads. 
* It also contains utilities for sending messages.
*
* @author  Kevin Bruhwiler
* @version 1.0
* @since   2019-06-11 
*/

package cs555.chiba.transport;

import cs555.chiba.service.Identity;
import cs555.chiba.util.Utilities;
import cs555.chiba.wireformats.EventFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TCPConnectionsCache implements AutoCloseable{
    private static final Logger logger = Logger.getLogger(TCPConnectionsCache.class.getName());

    private ConcurrentHashMap<Identity, TCPReceiverThread> receiverThreads = new ConcurrentHashMap<>();
    private ConcurrentHashMap<UUID,TCPSender> senders = new ConcurrentHashMap<>();

    private UUID registryID;

    public TCPConnectionsCache() {
       this.registryID = UUID.nameUUIDFromBytes("registry".getBytes());
    }

    public UUID getRegistryID() {
        return registryID;
    }

    /**
     * Adds a receiver thread to the cache
     */
    public void addReceiverThread(Identity ident, TCPReceiverThread receiver){
       Thread receiverThread = new Thread(receiver);
       receiverThread.start();
       TCPReceiverThread old = this.receiverThreads.putIfAbsent(ident, receiver);

       if (old != null) {
          old.close();
       }
    }

   public void addConnection(String host, int port, UUID id, EventFactory factory) {
      Socket sock = null;

      try {
         sock = new Socket(host, port);
         addSender(new TCPSender(sock), id);
         Identity ident = Identity.builder().withHost(host).withPort(port).build();
         addReceiverThread(ident, new TCPReceiverThread(sock, factory));
      }
      catch (IOException e) {
         logger.log(Level.SEVERE, "Failed to add connection to [" + host + "]", e);
         Utilities.closeQuietly(sock);
      }
   }

   /**
    * Adds a sender to the cache with the default ID
    * @param sender A reference to the sender object
    */
   public void addSender(TCPSender sender, UUID ID) {

      Thread senderThread = new Thread(sender);
      senderThread.start();
      TCPSender old = senders.putIfAbsent(ID, sender);

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
     * @param ID The ID of the sender to be removed
     */
    public void removeSender(UUID ID){
         senders.remove(ID);
    }

    /**
     * Check if we have a sender with the given ID
     * @returns boolean True if the sender exists
     */
    public boolean sendersContains(UUID item){
        return senders.containsKey(item);
    }

    /**
     * Send a message to the sender with the given ID
     * @param ID The ID of the recipient
     * @param message The serialized message to be sent
     */
    public void send(UUID ID, byte[] message){
        senders.get(ID).addMessage(message);
    }
    
    /**
     * Send a message a random sender
     * @param message The serialized message to be sent
     */
    public void sendToRandom(byte[] message){
    	getRandomSender().addMessage(message);
    }
    
    /**
     * Send a message to the sender with the given ID
     * @param exclude The ID of the excluded recipient
     * @param message The serialized message to be sent
     */
    public void sendToRandom(byte[] message, UUID exclude){
    	Random generator = new Random();
    	Object[] keys = senders.keySet().toArray();
    	
    	UUID key;
    	do {
    		key = (UUID) keys[generator.nextInt(keys.length)];
    	} while(key == exclude);
    	
    	TCPSender randomSender = senders.get(key);
    	randomSender.addMessage(message);
    }
    
    /**
     * Convenience to send a message over every connection in the cache
     * @param message The serialized message to be sent
     */
    public void sendAll(byte[] message){
        for (UUID key : senders.keySet()) {
        	if(key != registryID)
        		this.send(key, message);
        }
    }

    /**
     * Convenience to send a message over every connection in the cache
     * except the registry
     * @param message The serialized message to be sent
     */
    public void sendAll(byte[] message, UUID exclude){
        for (UUID key : senders.keySet()) {
        	if(key != registryID && key != exclude)
        		this.send(key, message);
        }
    }

    /**
     * Convenience to send a message to the registry
     * @param message The serialized message to be sent
     */
    public void send(byte[] message){
        senders.get(registryID).addMessage(message);
    }

    public List<String> listConnections() {
       return Collections.list(this.receiverThreads.keys()).stream().map(Identity::getIdentityKey).collect(Collectors.toList());
    }

    @Override
    public void close() {
        this.receiverThreads.values().forEach(TCPReceiverThread::close);
        this.senders.values().forEach(TCPSender::close);
    }

    public UUID getRegistryId() {
        return this.registryID;
    }
}
