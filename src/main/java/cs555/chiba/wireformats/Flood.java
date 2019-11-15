/**
* The SampleMessage class shows the outline for how
* to create new message types.
*
* @author  Kevin Bruhwiler
* @version 1.0
* @since   2019-06-11 
*/

package cs555.chiba.wireformats;

import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.UUID;


public class Flood implements Event{

    private final int type = Protocol.FLOOD.ordinal();
    private final int SIZE_OF_INT = 4;
    private UUID ID;
    private UUID senderID;
    private String target;
    private int hopLimit;
    private int currentHop;
    private Socket socket;
    private int totalDevicesWithMetric = 0;

    /**
     * This constructor should be used when creating a message
     * that will be sent
     * @param ID An number representing something
     * @param currentHop The current number of hops the message has made
     * @param hopLimit The maximum number of hops this message can make
     */
    public Flood(UUID ID, UUID senderID, String target, int currentHop, int hopLimit){
        this.ID = ID;
        this.senderID = senderID;
        this.currentHop = currentHop;
        this.hopLimit = hopLimit;
        this.target = target;
    }

    /**
     * This constructor should be used when deserializing a received message
     * @param message The serialized message
     * @param socket  The socket this message was received from
     */
    public Flood(byte[] message, Socket socket){ 
    	ByteBuffer b = ByteBuffer.allocate(message.length).put(message);
    	b.rewind();
        b.get();
        int IDlen = b.getInt();
        byte[] IDbytes = new byte[IDlen]; 
        b.get(IDbytes);
        ID = UUID.fromString(new String(IDbytes));
        int senderIDlen = b.getInt();
        byte[] senderIDbytes = new byte[senderIDlen]; 
        b.get(senderIDbytes);
        senderID = UUID.fromString(new String(senderIDbytes));
        int targetLen = b.getInt();
        byte[] targetBytes = new byte[targetLen]; 
        b.get(targetBytes);
        target = new String(targetBytes);
        currentHop = b.getInt();
        hopLimit = b.getInt();
        totalDevicesWithMetric = b.getInt();
        this.socket = socket;
    }

    /**
     * Serializes the message
     * @return byte[] The serialized message
     */
    public byte[] getBytes(){
    	byte[] IDbytes = ID.toString().getBytes();
    	byte[] senderIDbytes = senderID.toString().getBytes();
    	byte[] targetBytes = target.getBytes();
    	ByteBuffer b = ByteBuffer.allocate(IDbytes.length+senderIDbytes.length+targetBytes.length+5*SIZE_OF_INT+1);
    	b.put((byte)type);
    	b.putInt(IDbytes.length);
    	b.put(IDbytes);
    	b.putInt(senderIDbytes.length);
    	b.put(senderIDbytes);
    	b.putInt(targetBytes.length);
    	b.put(targetBytes);
    	b.putInt(currentHop);
    	b.putInt(hopLimit);
    	b.putInt(totalDevicesWithMetric);
        return b.array();
    }

    public int getType(){
        return type;
    }
    
    public UUID getID(){
        return ID;
    }
    
    public UUID getSenderID() {
    	return senderID;
    }
    
    public String getTarget() {
    	return target;
    }
    
    public int getCurrentHop() {
    	return currentHop;
    }
    
    public int getHopLimit(){
        return hopLimit;
    }

    public Socket getSocket() {
    	return socket;
    }

    public int getTotalDevicesWithMetric() {
        return totalDevicesWithMetric;
    }

    public void setTotalDevicesWithMetric(int totalDevicesWithMetric) {
        this.totalDevicesWithMetric = totalDevicesWithMetric;
    }
}