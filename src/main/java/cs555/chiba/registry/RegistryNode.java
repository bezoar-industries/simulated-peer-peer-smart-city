package cs555.chiba.registry;

import cs555.chiba.service.Identity;
import cs555.chiba.service.ServiceNode;
import cs555.chiba.util.Utilities;
import cs555.chiba.wireformats.Event;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author mmuller
 *
 * Start up the discovery node that returns a random node to folks who need access to the system
 */
public class RegistryNode extends ServiceNode {

   private static final Logger logger = Logger.getLogger(RegistryNode.class.getName());

   public static void main(String[] args) {
      try {
         RegistryNode node = parseArguments(args);
         startup(node.getIdentity().getPort(), node, RegistryCommands.getDiscoveryCommands(), "Discovery->");
      }
      catch (Exception e) {
         logger.log(Level.SEVERE, "Startup failed", e);
      }
   }

   private static RegistryNode parseArguments(String[] args) throws UnknownHostException {

      if (!Utilities.checkArgCount(1, args)) {
         throw new IllegalArgumentException("Discovery Node requires 1 arguments:  port-num");
      }

      int port = Utilities.parsePort(args[0]);
      Identity ident = Identity.builder().withHost(InetAddress.getLocalHost().getHostAddress()).withPort(port).build();

      return new RegistryNode(ident);
   }

   public RegistryNode(Identity ident) {
      super();
      setIdentity(ident);
   }

   @Override public void onEvent(Event event) {
      logger.info("We got a message! [" + event + "]");
   }
}
