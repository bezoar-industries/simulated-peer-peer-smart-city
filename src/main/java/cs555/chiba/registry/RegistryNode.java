package cs555.chiba.registry;

import cs555.chiba.service.Identity;
import cs555.chiba.service.ServiceNode;
import cs555.chiba.util.Utilities;
import cs555.chiba.wireformats.Event;
import cs555.chiba.wireformats.RegisterMessage;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

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

      logger.info("Starting up as [" + ident.getIdentityKey() + "]");
      return new RegistryNode(ident);
   }

   private final RegisteredPeers registry = new RegisteredPeers();

   public RegistryNode(Identity ident) {
      super();
      setIdentity(ident);
   }

   @Override public void onEvent(Event event) {
      logger.info("We got a message! [" + event + "]");
      {
         if (event instanceof RegisterMessage) {
            handle((RegisterMessage) event);
         }
         else {
            logger.severe("Cannot handle message [" + event.getClass().getSimpleName() + "]");
         }
      }
   }

   private void handle(RegisterMessage event) {
      this.registry.register(event);
   }

   public RegisteredPeers getRegistry() {
      return registry;
   }
}
