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
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

import cs555.chiba.transport.TCPSender;


public class TCPConnectionsCache{
    private ArrayList<Thread> recieverThreads = new ArrayList<>();
    private ConcurrentHashMap<UUID,TCPSender> senders = new ConcurrentHashMap<UUID,TCPSender>();

    private UUID registryID;

    public TCPConnectionsCache(){}

    /**
     * Initialize the cache with a sender 
     * A convenience for peers connecting to a registry
     * @param sender A reference to the sender object
     */
    public TCPConnectionsCache(TCPSender sender){
        Thread senderThread = new Thread(sender);
        senderThread.start();
        this.registryID = UUID.nameUUIDFromBytes("registry".getBytes());
        this.addSender(sender, registryID);
    }

    /**
     * Adds a receiver thread to the cache
     * @param t A receiver thread
     */
    public synchronized void addRecieverThread(Thread t){
        recieverThreads.add(t);
    }

    /**
     * Adds a sender to the cache with the default ID
     * @param sender A reference to the sender object
     */
    public void addSender(TCPSender sender, UUID ID){
    	Thread senderThread = new Thread(sender);
        senderThread.start();
        senders.putIfAbsent(ID, sender);
    }
    
    /**
     * Adds a sender to the cache with the specified ID
     * @param ID The ID of the sender
     * @param sender A reference to the sender object
     */
    public void addSender(UUID ID, TCPSender sender){
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
    	TCPSender randomSender = (TCPSender) values[generator.nextInt(values.length)];
    	return randomSender;
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
     * @param m The serialized message to be sent
     */
    public void send(UUID ID, byte[] message){
        senders.get(ID).addMessage(message);
    }
    
    /**
     * Send a message to the sender with the given ID
     * @param ID The ID of the recipient
     * @param m The serialized message to be sent
     */
    public void sendToRandom(byte[] message){
    	getRandomSender().addMessage(message);
    }
    
    /**
     * Send a message to the sender with the given ID
     * @param ID The ID of the recipient
     * @param m The serialized message to be sent
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
     * except the registry
     * @param m The serialized message to be sent
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
     * @param m The serialized message to be sent
     */
    public void sendAll(byte[] message, UUID exclude){
        for (UUID key : senders.keySet()) {
        	if(key != registryID && key != exclude)
        		this.send(key, message);
        }
    }

    /**
     * Convenience to send a message to the registry
     * @param m The serialized message to be sent
     */
    public void send(byte[] message){
        senders.get(registryID).addMessage(message);
    }
}
