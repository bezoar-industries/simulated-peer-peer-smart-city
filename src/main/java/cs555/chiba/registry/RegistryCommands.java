package cs555.chiba.registry;

import cs555.chiba.overlay.network.NetworkMap;
import cs555.chiba.overlay.network.NetworkMapTransformer;
import cs555.chiba.overlay.network.Vertex;
import cs555.chiba.service.Commands;
import cs555.chiba.service.Identity;
import cs555.chiba.service.ServiceNode;
import cs555.chiba.util.Utilities;
import cs555.chiba.wireformats.Flood;
import cs555.chiba.wireformats.GossipQuery;
import cs555.chiba.wireformats.InitiateConnectionsMessage;
import cs555.chiba.wireformats.RandomWalk;
import cs555.chiba.wireformats.ShutdownMessage;

import java.io.File;
import java.util.List;
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
         logger.info(listPeers(registryNode));
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

      builder.registerCommand("gossiptype0", args -> {
         if (!Utilities.checkArgCount(1, args)) {
            throw new IllegalArgumentException("Gossiping type 0 requires 1 argument:  " + "metric-to-collect");
         }

         sendGossipingRequest(args[0], registryNode, 0);
         return null;
      });
      
      builder.registerCommand("gossiptype1", args -> {
          if (!Utilities.checkArgCount(1, args)) {
             throw new IllegalArgumentException("Gossiping type 1 requires 1 argument:  " + "metric-to-collect");
          }

          sendGossipingRequest(args[0], registryNode, 1);
          return null;
       });

      builder.registerCommand("buildoverlay", args -> { // build a random overlay with the current peers
         if (!Utilities.checkArgCount(2, args)) {
            throw new IllegalArgumentException("Build Overlay requires 2 arguments:  min-connections-per-peer max-connections-per-peer");
         }

         int min = Utilities.quietlyParseInt(args[0], 1);
         int max = Utilities.quietlyParseInt(args[1], 2);
         logger.info(buildOverlay(min, max, registryNode));
         return null;
      });

      builder.registerCommand("exportoverlay", args -> { // export an overlay
         if (!Utilities.checkArgCount(1, args)) {
            throw new IllegalArgumentException("Export Overlay requires 1 arguments: path-to-export-file");
         }

         logger.info(exportOverlay(args[0], registryNode));
         return null;
      });

      builder.registerCommand("importoverlay", args -> { // import an overlay
         if (!Utilities.checkArgCount(1, args)) {
            throw new IllegalArgumentException("Import Overlay requires 1 arguments: path-to-import-file");
         }

         logger.info(importOverlay(args[0], registryNode));
         return null;
      });

      builder.registerCommand("connectpeers", args -> { // connect the peers
         if (registryNode.getNetworkMap() == null) {
            throw new IllegalArgumentException("Overlay is empty.  Call buildoverlay.");
         }
         else {
            connectPeers(registryNode);
         }
         return null;
      });

      builder.registerCommand("shutdown", args -> { // connect the peers
         shutdown(registryNode);
         return null;
      });

      return builder.build();
   }

   private static void sendRandomWalkRequest(String metric, RegistryNode registryNode) {
      RandomWalk request = new RandomWalk(UUID.randomUUID(), registryNode.getIdentity(), registryNode.getIdentity(), metric, 0, 10);
      registryNode.addRequest(request.getID());
      logger.info("Sending random Walk request");
      registryNode.getTcpConnectionsCache().sendToRandom(request.getBytes());
   }

   private static void sendGossipingRequest(String metric, RegistryNode registryNode, int type) {
      GossipQuery request = new GossipQuery(UUID.randomUUID(), registryNode.getIdentity(), registryNode.getIdentity(), metric, 0, 10, type);
      registryNode.addRequest(request.getID());
      logger.info("Sending Gossiping request");
      registryNode.getTcpConnectionsCache().sendToRandom(request.getBytes());
   }

   private static void sendFloodingRequest(String metric, RegistryNode registryNode) {
      Flood request = new Flood(UUID.randomUUID(), registryNode.getIdentity(), registryNode.getIdentity(), metric, 0, 10);
      registryNode.addRequest(request.getID());
      logger.info("Sending Flooding request");
      registryNode.getTcpConnectionsCache().sendToRandom(request.getBytes());
   }

   /**
    * Who's in our system
    * @param registryNode
    */
   private static String listPeers(RegistryNode registryNode) {
      StringBuffer out = new StringBuffer("Registered Peers: \n");
      List<Identity> peers = registryNode.getRegistry().listRegisteredPeers();
      peers.forEach(ident -> {
         out.append(ident.getIdentityKey()).append("\n");
      });
      out.append("Total Registered Peers: ").append(peers.size()).append("\n");

      return out.toString();
   }

   /**
    * Create an Overlay
    */
   private static String buildOverlay(int minConnections, int maxConnections, RegistryNode registryNode) {
      StringBuffer out = new StringBuffer("Building Overlay: \n");
      out.append(registryNode.buildOverlay(minConnections, maxConnections));
      return out.toString();
   }

   /**
    * Export an Overlay
    */
   private static String exportOverlay(String exportPath, RegistryNode registryNode) {
      StringBuilder out = new StringBuilder("Exporting Overlay: \n");
      try {
         NetworkMap net = registryNode.getNetworkMap();

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

   /**
    * Import an Overlay
    */
   private static String importOverlay(String importPath, RegistryNode registryNode) {
      StringBuilder out = new StringBuilder("Importing Overlay: \n");
      try {
         File file = new File(importPath);

         if (!file.exists() || file.isDirectory() || !file.canRead()) {
            out.append("Cannot read file [").append(file.getAbsolutePath()).append("]");
         }
         else {
            NetworkMapTransformer trans = new NetworkMapTransformer(file);
            NetworkMap net = trans.applyRegisteredNodes(registryNode.getRegistry().listRegisteredPeers());
            registryNode.setNetworkMap(net);
            out.append("Import Succeeded: [").append(net.size()).append("] vertices and [").append(net.getFullEdgeList().size()).append("] edges\n");
         }
      }
      catch (Exception e) {
         out.append("Import Failed \n");
         logger.log(Level.SEVERE, "Import Failed", e);
      }
      return out.toString();
   }

   private static void connectPeers(RegistryNode registryNode) {
      logger.info("Connecting Peers: \n");

      List<Vertex> vertices = registryNode.getNetworkMap().getVertices();

      for (Vertex vertex : vertices) {
         try {
            logger.info("Setting up peer: [" + vertex.getName().getIdentityKey() + "] \n");
            InitiateConnectionsMessage message = new InitiateConnectionsMessage(vertex.getConnectionList());
            registryNode.getTcpConnectionsCache().sendSingle(vertex.getName(), message.getBytes());
         }
         catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to set up peer: [" + vertex.getName().getIdentityKey() + "]", e);

         }
      }

      logger.info("Finished connecting peers.  If any failed, rerun this command. \n");
   }

   private static void shutdown(RegistryNode registryNode) {
      logger.info("Shutting Down Peers: \n");

      List<Identity> peers = registryNode.getRegistry().listRegisteredPeers();

      for (Identity peer : peers) {
         try {
            logger.info("Shutting down peer: [" + peer.getIdentityKey() + "] \n");
            ShutdownMessage message = new ShutdownMessage();
            registryNode.getTcpConnectionsCache().sendSingle(peer, message.getBytes());
         }
         catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to shutdown peer: [" + peer + "]", e);
         }
      }

      logger.info("Finished shutting down peers.\n");
      ServiceNode.getThisNode().shutdown();
   }
}
