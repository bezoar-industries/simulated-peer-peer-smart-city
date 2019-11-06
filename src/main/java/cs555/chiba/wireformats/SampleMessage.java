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


public class SampleMessage implements Event{

    private final int type = Protocol.SAMPLE_MESSAGE;
    private int num;
    private Socket socket;

    /**
     * This constructor should be used when creating a message
     * that will be sent
     * @param num An number representing something
     */
    public SampleMessage(int num){
        this.num = num;
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
        num = b.getInt();
        this.socket = socket;
    }

    /**
     * Serializes the message
     * @return byte[] The serialized message
     */
    public byte[] getBytes(){
    	ByteBuffer b = ByteBuffer.allocate(5);
    	b.put((byte)type);
    	b.putInt(num);
        return b.array();
    }

    public int getType(){
        return type;
    }
    
    public int getNum(){
        return num;
    }

    public Socket getSocket() {
    	return socket;
    }
}