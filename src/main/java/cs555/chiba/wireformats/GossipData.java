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


public class GossipData implements Event{

    private final int type = Protocol.GOSSIP_DATA.ordinal();
    private final int SIZE_OF_INT = 4;
    private Identity senderID;
    private HashMap<String,Integer> devices;
    private Socket socket;

    /**
     * This constructor should be used when creating a message
     * that will be sent
     */
    public GossipData(Identity senderID, HashMap<String,Integer> devices){
        this.senderID = senderID;
        this.devices = devices;
    }

    /**
     * This constructor should be used when deserializing a received message
     * @param message The serialized message
     * @param socket  The socket this message was received from
     */
    public GossipData(byte[] message, Socket socket){ 
    	ByteBuffer b = ByteBuffer.allocate(message.length).put(message);
    	b.rewind();
        b.get();
       int senderId = b.getInt();
       byte[] senderBytes = new byte[senderId];
       b.get(senderBytes);
       senderID = Identity.builder().withIdentityKey(new String(senderBytes)).build();
        int numDevices = b.getInt();
        devices = new HashMap<String,Integer>();
        for(int i = 0; i < numDevices; i++) {
        	int deviceLength = b.getInt();
        	byte[] deviceBytes = new byte[deviceLength]; 
        	b.get(deviceBytes);
        	int dist = b.getInt();
        	devices.put(new String(deviceBytes), dist);
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
    	for(Map.Entry<String, Integer> device : devices.entrySet()) {
    		byte[] deviceBytes = device.getKey().getBytes();
    		size += deviceBytes.length + 2*SIZE_OF_INT;
    	}
    	ByteBuffer b = ByteBuffer.allocate(size);
    	b.put((byte)type);
    	b.putInt(senderIDbytes.length);
    	b.put(senderIDbytes);
    	b.putInt(devices.size());
    	for(Map.Entry<String, Integer> device : devices.entrySet()) {
    		b.putInt(device.getKey().getBytes().length);
    		b.put(device.getKey().getBytes());
    		b.putInt(device.getValue());
    	}
        return b.array();
    }

    public int getType(){
        return type;
    }
    
    public Identity getSenderID() {
    	return senderID;
    }
    
    public HashMap<String,Integer> getDevices() {
    	return devices;
    }
    
    public Socket getSocket() {
    	return socket;
    }
}