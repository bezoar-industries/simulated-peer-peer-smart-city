/**
* The SampleMessage class shows the outline for how
* to create new message types.
*
* @author  Kevin Bruhwiler
* @version 1.0
* @since   2019-06-11 
*/

package cs555.chiba.wireformats;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.UUID;

public class SampleMessage implements Event{

    private final int type = Protocol.SAMPLE_MESSAGE;
    private UUID id;
    private Socket socket;

    /**
     * This constructor should be used when creating a message
     * that will be sent
     * @param num An number representing something
     */
    public SampleMessage(UUID num){
        this.id = num;
    }

    protected void sendString(String field, ByteBuffer output) {
        byte[] fieldBytes = field.getBytes();
        int fieldLength = fieldBytes.length;
        output.putInt(fieldLength);
        output.put(fieldBytes);
    }

    protected String readString(ByteBuffer input) {
        int fieldLength = input.getInt();
        byte[] fieldBytes = new byte[fieldLength];
        input.get(fieldBytes);
        return new String(fieldBytes);
    }

    /**
     * This constructor should be used when deserializing a received message
     * @param message The serialized message
     * @param socket  The socket this message was received from
     */
    public SampleMessage(byte[] message, Socket socket){ 
    	ByteBuffer b = ByteBuffer.allocate(message.length).put(message);
    	b.rewind();
        b.get();
        String id = readString(b);
        this.id = UUID.fromString(id);
        this.socket = socket;
    }

    /**
     * Serializes the message
     * @return byte[] The serialized message
     */
    public byte[] getBytes(){
    	ByteBuffer b = ByteBuffer.allocate(1 + 4 + this.id.toString().length());
    	b.put((byte)type);
    	sendString(this.id.toString(), b);
    	return b.array();
    }

    public int getType(){
        return type;
    }
    
    public UUID getNum(){
        return this.id;
    }

    public Socket getSocket() {
    	return socket;
    }

    @Override public String toString() {
        return "SampleMessage{" + "type=" + type + ", id=" + id + '}';
    }
}