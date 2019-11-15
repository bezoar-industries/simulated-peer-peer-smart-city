package cs555.chiba.registry;

import cs555.chiba.service.Commands;
import cs555.chiba.service.ServiceNode;
import cs555.chiba.wireformats.Flood;
import cs555.chiba.wireformats.GossipQuery;
import cs555.chiba.wireformats.RandomWalk;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * @author mmuller
 *
 * Available Discovery Node commands
 */
class RegistryCommands {

   private static final Logger logger = Logger.getLogger(RegistryCommands.class.getName());

   static Commands getDiscoveryCommands(RegistryNode registryNode) {
      Commands.Builder builder = Commands.builder();
      builder.registerCommand("listpeers", args -> { // list all the registered nodes
         logger.info(listPeers());
         return null;
      });

      builder.registerCommand("randomWalk", args -> { // list all the registered nodes
         sendRandomWalkRequest(args[0], registryNode);
         return null;
      });

      builder.registerCommand("gossip", args -> { // list all the registered nodes
         sendFloodingRequest(args[0], registryNode);
         return null;
      });

      builder.registerCommand("flood", args -> { // list all the registered nodes
         sendGossipingRequest(args[0], registryNode);
         return null;
      });

      return builder.build();
   }

   private static void sendRandomWalkRequest(String metric, RegistryNode registryNode) {
      RandomWalk request = new RandomWalk(UUID.randomUUID(), registryNode.getTcpConnectionsCache().getRegistryID(), metric, 0, 10);
      logger.info("Sending random Walk request");
      registryNode.getTcpConnectionsCache().sendToRandom(request.getBytes());
   }

   private static void sendGossipingRequest(String metric, RegistryNode registryNode) {
      GossipQuery request = new GossipQuery(UUID.randomUUID(), registryNode.getTcpConnectionsCache().getRegistryID(), metric, 0, 10);
      logger.info("Sending Gossiping request");
      registryNode.getTcpConnectionsCache().sendToRandom(request.getBytes());
   }

   private static void sendFloodingRequest(String metric, RegistryNode registryNode) {
      Flood request = new Flood(UUID.randomUUID(), registryNode.getTcpConnectionsCache().getRegistryID(), metric, 0, 10);
      logger.info("Sending Flooding request");
      registryNode.getTcpConnectionsCache().sendToRandom(request.getBytes());
   }

   /**
    * Who's in our system
    */
   private static String listPeers() {
      StringBuffer out = new StringBuffer("Registered Peers: \n");
      ServiceNode.getThisNode(RegistryNode.class).getRegistry().listRegisteredPeers().forEach(ident -> {
         out.append(ident).append("\n");
      });

      return out.toString();
   }
}
