/**
 * The EventFactory is a singleton class which
 * passes messages to nodes.
 *
 * @author Kevin Bruhwiler
 * @version 1.0
 * @since 2019-06-11
 */

package cs555.chiba.wireformats;

import cs555.chiba.service.ServiceNode;

import java.io.IOException;
import java.net.Socket;

public class EventFactory {

   private static EventFactory instance;
   private ServiceNode n;

   //private constructor to avoid client applications to use constructor
   private EventFactory() {
   }

   /**
    * Returns the singleton instance of the EventFactory
    * @param n The node associated with this factory
    * @return EventFactory The instance
    */
   public static EventFactory getInstance(ServiceNode n) {
      if (instance == null) {
         synchronized (EventFactory.class) {
            if (instance == null) {
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
         case INTRODUCTION:
            this.n.onEvent(new IntroductionMessage(message, socket));
            break;
         case REGISTER:
            this.n.onEvent(new RegisterMessage(message));
            break;
         case RANDOM_WALK:
            this.n.onEvent(new RandomWalk(message, socket));
            break;
         case FLOOD:
            this.n.onEvent(new Flood(message, socket));
            break;
         case GOSSIP_QUERY:
            this.n.onEvent(new GossipQuery(message, socket));
            break;
         case GOSSIP_DATA:
             this.n.onEvent(new GossipData(message, socket));
             break;
         case GOSSIP_ENTRIES:
             this.n.onEvent(new GossipEntries(message, socket));
             break;
         case INITIATE_CONNECTIONS:
            this.n.onEvent(new InitiateConnectionsMessage(message));
            break;
         case SHUTDOWN:
            this.n.onEvent(new ShutdownMessage(message));
            break;
         default:
            System.out.println("Event Factory cannot recognize message type [" + type + "]");
      }
   }
}