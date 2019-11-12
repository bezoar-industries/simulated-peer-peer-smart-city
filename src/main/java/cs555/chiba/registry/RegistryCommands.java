package cs555.chiba.registry;

import cs555.chiba.service.Commands;
import cs555.chiba.service.ServiceNode;

import java.util.logging.Logger;

/**
 * @author mmuller
 *
 * Available Discovery Node commands
 */
class RegistryCommands {

   private static final Logger logger = Logger.getLogger(RegistryCommands.class.getName());

   static Commands getDiscoveryCommands() {
      return Commands.builder().registerCommand("listpeers", args -> { // list all the registered nodes
         logger.info(listPeers());
         return null;
      }).build();
   }

   /**
    * Who's in our system
    */
   private static String listPeers() {
      StringBuffer out = new StringBuffer("Registered Peers: \n");
      ServiceNode.getThisNode().getTcpConnectionsCache().listConnections().forEach(ident -> {
         out.append(ident).append("\n");
      });

      return out.toString();
   }
}
