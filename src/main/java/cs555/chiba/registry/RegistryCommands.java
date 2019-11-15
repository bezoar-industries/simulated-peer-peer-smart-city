package cs555.chiba.registry;

import cs555.chiba.overlay.network.NetworkMap;
import cs555.chiba.overlay.network.NetworkMapTransformer;
import cs555.chiba.service.Commands;
import cs555.chiba.service.ServiceNode;
import cs555.chiba.util.Utilities;
import cs555.chiba.wireformats.Flood;
import cs555.chiba.wireformats.GossipQuery;
import cs555.chiba.wireformats.RandomWalk;

import java.io.File;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author mmuller
 *
 * Available Registry Node commands
 */
class RegistryCommands {

   private static final Logger logger = Logger.getLogger(RegistryCommands.class.getName());

   static Commands getRegistryCommands(RegistryNode registryNode) {
      Commands.Builder builder = Commands.builder();
      builder.registerCommand("listpeers", args -> { // list all the registered nodes
         logger.info(listPeers());
         return null;
      });

      builder.registerCommand("randomWalk", args -> {
         if (!Utilities.checkArgCount(1, args)) {
            throw new IllegalArgumentException("Random Walk requires 1 argument:  " + "metric-to-collect");
         }

         sendRandomWalkRequest(args[0], registryNode);
         return null;
      });

      builder.registerCommand("flood", args -> {
         if (!Utilities.checkArgCount(1, args)) {
            throw new IllegalArgumentException("Flooding requires 1 argument:  " + "metric-to-collect");
         }

         sendFloodingRequest(args[0], registryNode);
         return null;
      });

      builder.registerCommand("gossip", args -> {
         if (!Utilities.checkArgCount(1, args)) {
            throw new IllegalArgumentException("Gossiping requires 1 argument:  " + "metric-to-collect");
         }

         sendGossipingRequest(args[0], registryNode);
         return null;
      });

      builder.registerCommand("buildoverlay", args -> { // build a random overlay with the current peers
         if (!Utilities.checkArgCount(2, args)) {
            throw new IllegalArgumentException("Build Overlay requires 2 arguments:  min-connections-per-peer max-connections-per-peer");
         }

         int min = Utilities.quietlyParseInt(args[0], 1);
         int max = Utilities.quietlyParseInt(args[1], 2);
         logger.info(buildOverlay(min, max));
         return null;
      });

      builder.registerCommand("exportoverlay", args -> { // export an overlay
         if (!Utilities.checkArgCount(1, args)) {
            throw new IllegalArgumentException("Export Overlay requires 1 arguments: path-to-export-file");
         }

         logger.info(exportOverlay(args[0]));
         return null;
      });

      return builder.build();
   }

   private static void sendRandomWalkRequest(String metric, RegistryNode registryNode) {
      RandomWalk request = new RandomWalk(UUID.randomUUID(), registryNode.getTcpConnectionsCache().getRegistryID(), metric, 0, 10);
      registryNode.addRequest(request.getID());
      logger.info("Sending random Walk request");
      registryNode.getTcpConnectionsCache().sendToRandom(request.getBytes());
   }

   private static void sendGossipingRequest(String metric, RegistryNode registryNode) {
      GossipQuery request = new GossipQuery(UUID.randomUUID(), registryNode.getTcpConnectionsCache().getRegistryID(), metric, 0, 10);
      registryNode.addRequest(request.getID());
      logger.info("Sending Gossiping request");
      registryNode.getTcpConnectionsCache().sendToRandom(request.getBytes());
   }

   private static void sendFloodingRequest(String metric, RegistryNode registryNode) {
      Flood request = new Flood(UUID.randomUUID(), registryNode.getTcpConnectionsCache().getRegistryID(), metric, 0, 10);
      registryNode.addRequest(request.getID());
      logger.info("Sending Flooding request");
      registryNode.getTcpConnectionsCache().sendToRandom(request.getBytes());
   }

   /**
    * Who's in our system
    */
   private static String listPeers() {
      StringBuffer out = new StringBuffer("Registered Peers: \n");
      ServiceNode.getThisNode(RegistryNode.class).getRegistry().listRegisteredPeers().forEach(ident -> {
         out.append(ident.getIdentityKey()).append("\n");
      });

      return out.toString();
   }

   /**
    * Create an Overlay
    */
   private static String buildOverlay(int minConnections, int maxConnections) {
      StringBuffer out = new StringBuffer("Building Overlay: \n");
      out.append(ServiceNode.getThisNode(RegistryNode.class).buildOverlay(minConnections, maxConnections));
      return out.toString();
   }

   /**
    * Export an Overlay
    */
   private static String exportOverlay(String exportPath) {
      StringBuffer out = new StringBuffer("Exporting Overlay: \n");
      try {
         NetworkMap net = ServiceNode.getThisNode(RegistryNode.class).getNetworkMap();

         if (net == null) {
            out.append("Overlay is empty.  Call buildoverlay first.");
         }
         else {
            NetworkMapTransformer trans = new NetworkMapTransformer(net);
            String edges = trans.export();
            File file = Utilities.writeFile(exportPath, edges);
            out.append("Export Succeeded: [").append(file.getAbsolutePath()).append("] \n");
         }
      }
      catch (Exception e) {
         out.append("Export Failed \n");
         logger.log(Level.SEVERE, "Export Failed", e);
      }
      return out.toString();
   }
}
