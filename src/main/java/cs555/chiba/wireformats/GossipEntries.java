/**
* The SampleMessage class shows the outline for how
* to create new message types.
*
* @author  Kevin Bruhwiler
* @version 1.0
* @since   2019-06-11 
*/

package cs555.chiba.wireformats;

import cs555.chiba.service.Identity;
import cs555.chiba.util.LRUCache;
import cs555.chiba.util.LRUCache.Entry;

import java.net.Socket;
import java.nio.ByteBuffer;

public class GossipEntries implements Event{

    private final int type = Protocol.GOSSIP_ENTRIES.ordinal();
    private final int SIZE_OF_INT = 4;
    private Identity senderID;
    private Entry[] devices;
    private Socket socket;

    /**
     * This constructor should be used when creating a message
     * that will be sent
     */
    public GossipEntries(Identity senderID, Entry[] devices){
        this.senderID = senderID;
        this.devices = devices;
    }

    /**
     * This constructor should be used when deserializing a received message
     * @param message The serialized message
     * @param socket  The socket this message was received from
     */
    public GossipEntries(byte[] message, Socket socket){ 
    	ByteBuffer b = ByteBuffer.allocate(message.length).put(message);
    	b.rewind();
        b.get();
       int senderId = b.getInt();
       byte[] senderBytes = new byte[senderId];
       b.get(senderBytes);
       senderID = Identity.builder().withIdentityKey(new String(senderBytes)).build();
        int numDevices = b.getInt();
        devices = new Entry[numDevices];
        for(int i = 0; i < numDevices; i++) {
        	int identityLength = b.getInt();
        	byte[] identityBytes = new byte[identityLength]; 
        	b.get(identityBytes);
        	int deviceLength = b.getInt();
        	byte[] deviceBytes = new byte[deviceLength]; 
        	b.get(deviceBytes);
        	Entry newEntry = new LRUCache(1).new Entry();
        	newEntry.value = Identity.builder().withIdentityKey(new String(identityBytes)).build();
        	newEntry.keyName = new String(deviceBytes);
        	devices[i] = newEntry;
        }
        this.socket = socket;
    }

    /**
     * Serializes the message
     * @return byte[] The serialized message
     */
    public byte[] getBytes(){
       byte[] senderIDbytes = senderID.getIdentityKey().getBytes();
    	int size = senderIDbytes.length + 2*SIZE_OF_INT + 1;
    	for(Entry device : devices) {
    		byte[] entryBytes = device.value.getIdentityKey().getBytes();
    		size += entryBytes.length + SIZE_OF_INT;
    		byte[] deviceBytes = device.keyName.getBytes();
    		size += deviceBytes.length + SIZE_OF_INT;
    	}
    	ByteBuffer b = ByteBuffer.allocate(size);
    	b.put((byte)type);
    	b.putInt(senderIDbytes.length);
    	b.put(senderIDbytes);
    	b.putInt(devices.length);
    	for(Entry device : devices) {
    		b.putInt(device.value.getIdentityKey().getBytes().length);
    		b.put(device.value.getIdentityKey().getBytes());
    		b.putInt(device.keyName.getBytes().length);
    		b.put(device.keyName.getBytes());
    	}
        return b.array();
    }

    public int getType(){
        return type;
    }
    
    public Identity getSenderID() {
    	return senderID;
    }
    
    public Entry[] getDevices() {
    	return devices;
    }

    public Socket getSocket() {
    	return socket;
    }
}