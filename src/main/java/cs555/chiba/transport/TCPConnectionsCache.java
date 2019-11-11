/**
* The TCPConnectionsCache stores references to all the sender/receiver threads. 
* It also contains utilities for sending messages.
*
* @author  Kevin Bruhwiler
* @version 1.0
* @since   2019-06-11 
*/

package cs555.chiba.transport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import cs555.chiba.service.Identity;
import cs555.chiba.transport.TCPSender;
import cs555.chiba.util.Utilities;

public class TCPConnectionsCache implements AutoCloseable{
    private static final Logger logger = Logger.getLogger(TCPConnectionsCache.class.getName());

    private ArrayList<Thread> receiverThreads = new ArrayList<>();
    private ConcurrentHashMap<Identity,TCPSender> senders = new ConcurrentHashMap<>();

    private Identity registryID;

    public TCPConnectionsCache(){}

    /**
     * Initialize the cache with a sender 
     * A convenience for peers connecting to a registry
     * @param sender A reference to the sender object
     */
    public TCPConnectionsCache(TCPSender sender, Identity ident){
        this.addSender(sender);
        this.registryID = ident;
    }

    /**
     * Adds a receiver thread to the cache
     * @param t A receiver thread
     */
    public synchronized void addReceiverThread(Thread t){
        receiverThreads.add(t);
    }

    /**
     * Adds a sender to the cache with the default ID
     * @param sender A reference to the sender object
     */
    public void addSender(TCPSender sender){
    	Thread senderThread = new Thread(sender);
        senderThread.start();
        Identity ident = Identity.builder().withSocketAddress(sender.getSocket().getRemoteSocketAddress()).build();
        senders.putIfAbsent(ident, sender);
    }
    
    /**
     * Adds a sender to the cache with the specified ID
     * @param ID The ID of the sender
     * @param sender A reference to the sender object
     */
    public void addSender(Identity ID, TCPSender sender){
    	Thread senderThread = new Thread(sender);
        senderThread.start();
        senders.putIfAbsent(ID, sender);
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
    public void removeSender(String ID){
         senders.remove(ID);
    }

    /**
     * Check if we have a sender with the given ID
     * @returns boolean True if the sender exists
     */
    public boolean sendersContains(String item){
        return senders.containsKey(item);
    }

    /**
     * Send a message to the sender with the given ID
     * @param ID The ID of the recipient
     * @param message The serialized message to be sent
     */
    public void send(Identity ID, byte[] message){
        senders.get(ID).addMessage(message);
    }

    /**
     * Convenience to send a message over every connection in the cache
     * @param message The serialized message to be sent
     */
    public void sendAll(byte[] message){
        for (Identity key : senders.keySet()) {
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

    public List<Identity> listConnections() {
        return Utilities.copy(senders.keySet());
    }

    @Override
    public void close() {
        this.receiverThreads.forEach(Thread::interrupt);
        this.senders.values().forEach(TCPSender::close);
    }
}
