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
import cs555.chiba.wireformats.ListPeersRequestMessage;
import cs555.chiba.wireformats.RandomWalk;
import cs555.chiba.wireformats.ShutdownMessage;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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
         if (!Utilities.checkArgCount(2, args)) {
            throw new IllegalArgumentException("Random Walk requires 2 arguments:  " + "metric-to-collect hop-limit");
         }

         sendRandomWalkRequest(args[0], Integer.parseInt(args[1]), registryNode);
         return null;
      });

      builder.registerCommand("flood", args -> {
         if (!Utilities.checkArgCount(2, args)) {
            throw new IllegalArgumentException("Flooding requires 2 arguments:  " + "metric-to-collect, hop-limit");
         }

         sendFloodingRequest(args[0], Integer.parseInt(args[1]), registryNode);
         return null;
      });

      builder.registerCommand("gossiptype0", args -> {
         if (!Utilities.checkArgCount(2, args)) {
            throw new IllegalArgumentException("Gossiping type 0 requires 2 arguments:  " + "metric-to-collect, hop-limit");
         }

         sendGossipingRequest(args[0], Integer.parseInt(args[1]), registryNode, 0);
         return null;
      });

      builder.registerCommand("gossiptype1", args -> {
         if (!Utilities.checkArgCount(2, args)) {
            throw new IllegalArgumentException("Gossiping type 1 requires 2 arguments:  " + "metric-to-collect, hop-limit");
         }

         sendGossipingRequest(args[0], Integer.parseInt(args[1]), registryNode, 1);
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

      builder.registerCommand("print-results", args -> {
         System.out.println(registryNode.getRequests());
         return null;
      });
      
      builder.registerCommand("export-results", args -> {
    	  if (!Utilities.checkArgCount(1, args)) {
              throw new IllegalArgumentException("Export Overlay requires 1 arguments: path-to-export-file");
           }
    	  logger.info(exportResults(args[0], registryNode.getRequests()));
          return null;
       });

      builder.registerCommand("clear-results", args -> {
         registryNode.clearRequests();
         return null;
      });

      builder.registerCommand("checkpeers", args -> {
         checkpeers(registryNode);
         return null;
      });

      builder.registerCommand("exportgephi", args -> {
         if (!Utilities.checkArgCount(1, args)) {
            throw new IllegalArgumentException("Export Overlay requires 1 arguments: path-to-export-file");
         }

         logger.info(exportOverlayForGephi(args[0], registryNode));
         return null;
      });


      return builder.build();
   }

   private static void sendRandomWalkRequest(String metric, int hopLimit, RegistryNode registryNode) {
      RandomWalk request = new RandomWalk(UUID.randomUUID(), registryNode.getIdentity(), registryNode.getIdentity(), metric, 0, hopLimit);
      registryNode.addRequest(request.getID(), "randomWalk");
      logger.info("Sending random Walk request");
      registryNode.getTcpConnectionsCache().sendSingle(registryNode.getRegistry().getRandomPeer(), request.getBytes());
   }

   private static void sendGossipingRequest(String metric, int hopLimit, RegistryNode registryNode, int type) {
      GossipQuery request = new GossipQuery(UUID.randomUUID(), registryNode.getIdentity(), registryNode.getIdentity(), metric, 0, hopLimit, type);
      if(type == 0) {
    	  registryNode.addRequest(request.getID(), "gossip_type_0");
      } else {
    	  registryNode.addRequest(request.getID(), "gossip_type_1");
      }
      logger.info("Sending Gossiping request");
      registryNode.getTcpConnectionsCache().sendSingle(registryNode.getRegistry().getRandomPeer(), request.getBytes());
   }

   private static void sendFloodingRequest(String metric, int hopLimit, RegistryNode registryNode) {
      Flood request = new Flood(UUID.randomUUID(), registryNode.getIdentity(), registryNode.getIdentity(), metric, 0, hopLimit);
      registryNode.addRequest(request.getID(), "flood");
      logger.info("Sending Flooding request");
      registryNode.getTcpConnectionsCache().sendSingle(registryNode.getRegistry().getRandomPeer(), request.getBytes());
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
            out.append("Overlay is empty.  Call buildoverlay or importoverlay first.");
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
   
   private static String exportResults(String exportPath, ConcurrentHashMap<UUID, ResultMetrics> results) {
	      StringBuilder out = new StringBuilder("Exporting Results: \n");
	      try {
	    	  FileWriter f = new FileWriter(exportPath);
	    	  f.write("ID,Total Hops,Total Devices,Devices with Metric,Max Hops,Hop Limit,Time Start,Time End,Type\n");
	    	  for(ResultMetrics m : results.values()) {
		    	  StringBuilder line = new StringBuilder("");
		    	  line.append(m.getRequestId().toString()).append(",");
		    	  line.append(m.getTotalNumberOfHops()).append(",");
		    	  line.append(m.getTotalNumberOfDevices()).append(",");
		    	  line.append(m.getTotalNumberOfDevicesWithMetric()).append(",");
		    	  line.append(m.getMaxHops()).append(",");
		    	  line.append(m.getHopLimit()).append(",");
		    	  line.append(m.getTimeQueryStarted()).append(",");
		    	  line.append(m.getTimeOfLastReceivedResultMessage()).append(",");
		    	  line.append(m.getTypeOfQuery()).append("\n");
		    	  f.write(line.toString());
	    	  }
	    	  f.close();
	      }
	      catch (Exception e) {
	         out.append("Export Failed \n");
	         logger.log(Level.SEVERE, "Export Failed", e);
	      }
	      out.append("Success!");
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
            InitiateConnectionsMessage message = new InitiateConnectionsMessage(vertex.getConnectionList(), vertex.getDeviceString());
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

   private static void checkpeers(RegistryNode registryNode) {
      logger.info("Checking Peers for Correctness...\n");
      registryNode.clearCheckCount();
      List<Vertex> vertices = registryNode.getNetworkMap().getVertices();

      for (Vertex vertex : vertices) {
         try {
            ListPeersRequestMessage message = new ListPeersRequestMessage();
            registryNode.getTcpConnectionsCache().sendSingle(vertex.getName(), message.getBytes());
         }
         catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to contact peer: [" + vertex.getName().getIdentityKey() + "]", e);
         }
      }
   }

   /**
    * Export an Overlay as a CSV that Gephi can read to visualize the network
    */
   private static String exportOverlayForGephi(String exportPath, RegistryNode registryNode) {
      StringBuilder out = new StringBuilder("Exporting Gephi Overlay: \n");
      try {
         NetworkMap net = registryNode.getNetworkMap();

         if (net == null) {
            out.append("Overlay is empty.  Call buildoverlay or importoverlay first.");
         }
         else {
            NetworkMapTransformer trans = new NetworkMapTransformer(net);
            String edges = trans.exportGephi();
            File file = Utilities.writeFile(exportPath, edges);
            out.append("Gephi Export Succeeded: [").append(file.getAbsolutePath()).append("] \n");
         }
      }
      catch (Exception e) {
         out.append("Gephi Export Failed \n");
         logger.log(Level.SEVERE, "Gephi Export Failed", e);
      }
      return out.toString();
   }
}
