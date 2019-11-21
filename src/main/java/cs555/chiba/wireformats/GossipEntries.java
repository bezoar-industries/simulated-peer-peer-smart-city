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

import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;


public class GossipEntries implements Event{

    private final int type = Protocol.GOSSIP_ENTRIES.ordinal();
    private final int SIZE_OF_INT = 4;
    private Identity senderID;
    private HashMap<Identity,String> devices;
    private Socket socket;

    /**
     * This constructor should be used when creating a message
     * that will be sent
     */
    public GossipEntries(Identity senderID, HashMap<Identity,String> devices){
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
       int senderIDlen = b.getInt();
       byte[] senderIDbytes = new byte[senderIDlen];
       b.get(senderIDbytes);
       int senderId = b.getInt();
       byte[] senderBytes = new byte[senderId];
       b.get(senderBytes);
       senderID = Identity.builder().withIdentityKey(new String(senderBytes)).build();
        int numDevices = b.getInt();
        devices = new HashMap<Identity,String>();
        for(int i = 0; i < numDevices; i++) {
        	int identityLength = b.getInt();
        	byte[] identityBytes = new byte[identityLength]; 
        	b.get(identityBytes);
        	int deviceLength = b.getInt();
        	byte[] deviceBytes = new byte[deviceLength]; 
        	b.get(deviceBytes);
        	devices.put(Identity.builder().withIdentityKey(new String(identityBytes)).build(), new String(deviceBytes));
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
    	for(Map.Entry<Identity,String> device : devices.entrySet()) {
    		byte[] entryBytes = device.getKey().getIdentityKey().getBytes();
    		size += entryBytes.length + SIZE_OF_INT;
    		byte[] deviceBytes = device.getValue().getBytes();
    		size += deviceBytes.length + SIZE_OF_INT;
    	}
    	ByteBuffer b = ByteBuffer.allocate(size);
    	b.put((byte)type);
    	b.putInt(senderIDbytes.length);
    	b.put(senderIDbytes);
    	b.putInt(devices.size());
    	for(Map.Entry<Identity,String> device : devices.entrySet()) {
    		b.putInt(device.getKey().getIdentityKey().getBytes().length);
    		b.put(device.getKey().getIdentityKey().getBytes());
    		b.putInt(device.getValue().getBytes().length);
    		b.put(device.getValue().getBytes());
    	}
        return b.array();
    }

    public int getType(){
        return type;
    }
    
    public Identity getSenderID() {
    	return senderID;
    }
    
    public HashMap<Identity,String> getDevices() {
    	return devices;
    }

    public Socket getSocket() {
    	return socket;
    }
}