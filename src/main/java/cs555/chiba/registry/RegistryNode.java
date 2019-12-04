package cs555.chiba.registry;

import cs555.chiba.overlay.network.NetworkMap;
import cs555.chiba.overlay.network.NetworkMapCheck;
import cs555.chiba.service.ServiceNode;
import cs555.chiba.util.Utilities;
import cs555.chiba.wireformats.Event;
import cs555.chiba.wireformats.Flood;
import cs555.chiba.wireformats.GossipQuery;
import cs555.chiba.wireformats.ListPeersResponseMessage;
import cs555.chiba.wireformats.RandomWalk;
import cs555.chiba.wireformats.RegisterMessage;

import java.net.UnknownHostException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RegistryNode extends ServiceNode {

   private static final Logger logger = Logger.getLogger(RegistryNode.class.getName());

   private final RegisteredPeers registry = new RegisteredPeers();
   private NetworkMap networkMap;
   private int port; // my startup port
   private NetworkMapCheck mapCheck;

   private ConcurrentHashMap<UUID, ResultMetrics> requests = new ConcurrentHashMap<>();

   public RegistryNode(int port) {
      super();
      this.port = port;
   }

   @Override public void onEvent(Event event) {
      if (event instanceof RegisterMessage) {
         handle((RegisterMessage) event);
      }
      else if (event instanceof RandomWalk) {
         RandomWalk randomWalkMessage = (RandomWalk) event;
         if (requests.containsKey(randomWalkMessage.getID())) {
            this.requests.put(randomWalkMessage.getID(), new ResultMetrics(randomWalkMessage.getID(), 0, 0, 0, 0, 0, "randomWalk"));
         }

         requests.get(randomWalkMessage.getID()).addResult(randomWalkMessage.getCurrentHop(), randomWalkMessage.getTotalDevicesChecked(), randomWalkMessage.getTotalDevicesWithMetric(), randomWalkMessage.getCurrentHop(), randomWalkMessage.getHopLimit());
      }
      else if (event instanceof GossipQuery) {
         GossipQuery gossipQueryMessage = (GossipQuery) event;
         if (!requests.containsKey(gossipQueryMessage.getID())) {
        	 if(gossipQueryMessage.getGossipType() == 0) {
        		 this.requests.put(gossipQueryMessage.getID(), new ResultMetrics(gossipQueryMessage.getID(), 0, 0, 0, 0, 0, "gossip_type_0"));
        	 } else {
        		 this.requests.put(gossipQueryMessage.getID(), new ResultMetrics(gossipQueryMessage.getID(), 0, 0, 0, 0, 0, "gossip_type_1"));
        	 }
         }

         requests.get(gossipQueryMessage.getID()).addResult(gossipQueryMessage.getCurrentHop(), gossipQueryMessage.getTotalDevicesChecked(), gossipQueryMessage.getTotalDevicesWithMetric(), gossipQueryMessage.getCurrentHop(), gossipQueryMessage.getHopLimit());
      }
      else if (event instanceof Flood) {
         Flood floodMessage = (Flood) event;

         if (!requests.containsKey(floodMessage.getID())) {
            this.requests.put(floodMessage.getID(), new ResultMetrics(floodMessage.getID(), 0, 0, 0, 0, 0, "flood"));
         }

         this.requests.get(floodMessage.getID()).addResult(floodMessage.getCurrentHop(), floodMessage.getTotalDevicesChecked(), floodMessage.getTotalDevicesWithMetric(), floodMessage.getCurrentHop(), floodMessage.getHopLimit());
      }
      else if (event instanceof ListPeersResponseMessage) {
         if (this.mapCheck != null) {
            this.mapCheck.handle((ListPeersResponseMessage) event);
         }
      }
      else {
         logger.severe("Cannot handle message [" + event.getClass().getSimpleName() + "]");
      }
   }

   public void addRequest(UUID requestId, String type) {
      ResultMetrics resultMetrics = new ResultMetrics(requestId, 0, 0, 0, 0, 0, type);
      requests.put(requestId, resultMetrics);
   }

   private void handle(RegisterMessage event) {
      this.registry.register(event);
   }

   public RegisteredPeers getRegistry() {
      return registry;
   }

   public NetworkMap getNetworkMap() {
      return networkMap;
   }

   public String buildOverlay(int minConnections, int maxConnections) {
      this.networkMap = new NetworkMap(this.registry.listRegisteredPeers(), minConnections, maxConnections);
      return "Successfully Created.  Next step is building the cluster.";
   }

   public void setNetworkMap(NetworkMap networkMap) {
      this.networkMap = networkMap;
   }

   public int getPort() {
      return this.port;
   }

   public ConcurrentHashMap<UUID, ResultMetrics> getRequests() {
      return requests;
   }

   public void clearRequests() {
      requests = new ConcurrentHashMap<>();
   }

   public static void main(String[] args) {
      try {
         RegistryNode node = parseArguments(args);
         startup(node.getPort(), node, RegistryCommands.getRegistryCommands(node), "Registry->");
      }
      catch (Exception e) {
         logger.log(Level.SEVERE, "Startup failed", e);
      }
   }

   private static RegistryNode parseArguments(String[] args) throws UnknownHostException {
      if (!Utilities.checkArgCount(1, args)) {
         throw new IllegalArgumentException("Registry Node requires 1 arguments:  port-num");
      }

      int port = Utilities.parsePort(args[0]);
      return new RegistryNode(port);
   }

   public void clearCheckCount() {
      this.mapCheck = new NetworkMapCheck(this.networkMap);
   }
}
