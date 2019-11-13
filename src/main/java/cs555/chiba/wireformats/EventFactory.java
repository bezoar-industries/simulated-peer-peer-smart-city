/**
* The EventFactory is a singleton class which
* passes messages to nodes.
*
* @author  Kevin Bruhwiler
* @version 1.0
* @since   2019-06-11 
*/

package cs555.chiba.wireformats;

import java.io.IOException;
import java.net.Socket;

import cs555.chiba.node.Node;


public class EventFactory {

    private static EventFactory instance;
    private Node n;

    //private constructor to avoid client applications to use constructor
    private EventFactory(){}

    /**
     * Returns the singleton instance of the EventFactory
     * @param n The node associated with this factory
     * @return EventFactory The instance
     */
    public static EventFactory getInstance(Node n){
        if(instance == null){
            synchronized (EventFactory.class) {
                if(instance == null){
                    instance = new EventFactory();
                    instance.n = n;
                }
            }
        }
        return instance;
    }

    /**
     * Deserializes a message and routes it to the associated Node's onEvent method
     * @param message The serialized message to be routed
     * @param socket The socket that the message was received over
     */
    public void processMessage(byte[] message, Socket socket) throws IOException {
        Protocol type = Protocol.values()[message[0]];
        switch (type) {
            case SAMPLE_MESSAGE:
                this.n.onEvent(new SampleMessage(message, socket));
                break;
            case INTRODUCTION:
                this.n.onEvent(new IntroductionMessage(message));
                break;
            case REGISTER:
                this.n.onEvent(new RegisterMessage(message));
                break;
            default:
                System.out.println("Unrecognised message type");
        }
    }
}