/**
 * The Peer class is the entry point for individual peers
 * and manages setup and event handling.
 *
 * @author Kevin Bruhwiler
 * @version 1.0
 * @since 2019-06-11
 */

package cs555.chiba.node;

import cs555.chiba.iotDevices.*;
import cs555.chiba.service.Identity;
import cs555.chiba.service.ServiceNode;
import cs555.chiba.util.LRUCache;
import cs555.chiba.util.LRUCache.Entry;
import cs555.chiba.util.Metric;
import cs555.chiba.util.Utilities;
import cs555.chiba.wireformats.Event;
import cs555.chiba.wireformats.Flood;
import cs555.chiba.wireformats.GossipData;
import cs555.chiba.wireformats.GossipEntries;
import cs555.chiba.wireformats.GossipQuery;
import cs555.chiba.wireformats.InitiateConnectionsMessage;
import cs555.chiba.wireformats.IntroductionMessage;
import cs555.chiba.wireformats.ListPeersRequestMessage;
import cs555.chiba.wireformats.ListPeersResponseMessage;
import cs555.chiba.wireformats.RandomWalk;
import cs555.chiba.wireformats.RegisterMessage;
import cs555.chiba.wireformats.ShutdownMessage;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Peer extends ServiceNode {

   private static final Logger logger = Logger.getLogger(Peer.class.getName());

   private Identity registryId;
   private LRUCache queryIDCache;
   private LRUCache gossipCache;
   private LRUCache gossipEntries;
   private HashMap<UUID, Metric> metrics;
   private List<IotDevice> connectedIotDevices;

   public Peer(String registryHost, int registryPort, int numberOfIoTDevices, int cacheSize) throws IOException {
      super();
      this.registryId = Identity.builder().withHost(registryHost).withPort(registryPort).build();
      this.createIotNetwork(numberOfIoTDevices);

      this.queryIDCache = new LRUCache(40);
      this.gossipCache = new LRUCache(cacheSize);
      this.gossipEntries = new LRUCache(cacheSize);
      this.metrics = new HashMap<>();
   }

   @Override protected void specialStartUp() {
      register();
   }

   private void register() {
      try {
         logger.info("Registering: [" + this.getIdentity().getIdentityName() + "] \n");
         IotTransformer trans = new IotTransformer(this.connectedIotDevices);
         RegisterMessage message = new RegisterMessage(this.getIdentity(), trans.getDeviceString());
         this.getTcpConnectionsCache().sendSingle(this.registryId, message.getBytes());
      }
      catch (Exception e) {
         throw new IllegalStateException("Unable to communicate with Registry.  Double check your host and registry before running again.", e);
      }
   }

   // these are overwritten by the devices sent by the overlay
   // We should either remove this push them to the Overlay.
   private void createIotNetwork(int numberOfIoTDevices) {
      if (numberOfIoTDevices == 0) {
         // if no number of IoT devices defined, then generate a random number between 3 and 30 devices
         // Upper bounds of 27 and then adding 3 to ensure the range above is followed
         this.connectedIotDevices = IotFactory.generateRandomDevices(3, 30);
      }
      else {
         this.connectedIotDevices = IotFactory.generateRandomDevices(1, numberOfIoTDevices);
      }
   }

   private Integer calculateTotalDevicesWithMetric(String metricName) {
      int devicesWithMetric = 0;
      for (IotDevice device : this.connectedIotDevices) {
         devicesWithMetric += device.getMetric(metricName);
      }
      return devicesWithMetric;
   }
   
   private ArrayList<String> getAllMetricNames(){
	   ArrayList<String> metricNames = new ArrayList<>();
	   for (IotDevice device : this.connectedIotDevices) {
		   metricNames.addAll(new ArrayList<>(Arrays.asList(device.getMetricNames())));
	   }
	   return metricNames;
   }

   /**
    * A handler for Flood messages
    * @param e The Flood message
    */
   private void handle(Flood e) {
      logger.info("Received flood message with ID: " + e.getID());

      Flood nextFloodMessage = new Flood(e.getID(), this.getIdentity(), e.getOriginatorId(), e.getTarget(), e.getCurrentHop() + 1, e.getHopLimit());
      nextFloodMessage.setTotalDevicesChecked(e.getTotalDevicesChecked());
      nextFloodMessage.setTotalDevicesWithMetric(e.getTotalDevicesWithMetric());
      byte[] m = nextFloodMessage.getBytes();

      synchronized(queryIDCache) {
	      if (queryIDCache.containsEntry(e.getID())) {
	         //We've already processed this query - ignore it
	         queryIDCache.putEntry(e.getID(), e.getSenderID());
	
	         this.getTcpConnectionsCache().sendSingle(e.getOriginatorId(), m);
	
	         return;
	      }
	
	      queryIDCache.putEntry(e.getID(), e.getSenderID());
      }
      //Check if queried data is here - if so, log appropriately
      metrics.put(e.getID(), new Metric(this.calculateTotalDevicesWithMetric(e.getTarget()), e.getCurrentHop()));

      if (e.getCurrentHop() + 1 < e.getHopLimit() || e.getHopLimit() == -1) {
         //If the message hasn't yet hit its hop limit

         // Get a random node to send the totals with this nodes stats.  Then combine in a list with the sender.
         // This list should be excluded from the initial message
         Identity randomNode = this.getTcpConnectionsCache().getRandomSender().getIdentity();

         nextFloodMessage.setTotalDevicesChecked(e.getTotalDevicesChecked() + connectedIotDevices.size());
         nextFloodMessage.setTotalDevicesWithMetric(e.getTotalDevicesWithMetric() + this.calculateTotalDevicesWithMetric(e.getTarget()));
         m = nextFloodMessage.getBytes();

         this.getTcpConnectionsCache().sendSingle(randomNode, m);

         List<Identity> nodesToExclude = new ArrayList<>();
         nodesToExclude.add(randomNode);
         nodesToExclude.add(e.getSenderID());

         nextFloodMessage.setTotalDevicesChecked(e.getTotalDevicesChecked());
         nextFloodMessage.setTotalDevicesWithMetric(e.getTotalDevicesWithMetric());
         m = nextFloodMessage.getBytes();

         this.getTcpConnectionsCache().sendAll(m, nodesToExclude);
      } else {
         // No more hops for the flooding to take

         nextFloodMessage.setTotalDevicesChecked(e.getTotalDevicesChecked() + connectedIotDevices.size());
         nextFloodMessage.setTotalDevicesWithMetric(e.getTotalDevicesWithMetric() + this.calculateTotalDevicesWithMetric(e.getTarget()));
         m = nextFloodMessage.getBytes();

         this.getTcpConnectionsCache().sendSingle(e.getOriginatorId(), m);

      }
   }

   /**
    * A handler for RandomWalk messages
    * @param e The RandomWalk message
    */
   private void handle(RandomWalk e) {
      logger.info("Received random walk message with ID: " + e.getID());

      RandomWalk nextRWMesage = new RandomWalk(e.getID(), this.getIdentity(), e.getOriginatorId(), e.getTarget(), e.getCurrentHop() + 1, e.getHopLimit());

      synchronized(queryIDCache) {
	      if (!queryIDCache.containsEntry(e.getID())) {
	         //We've already processed this query - don't process it again (but still forward it)
	         //Check if queried data is here - if so, log appropriately
	         metrics.put(e.getID(), new Metric(this.calculateTotalDevicesWithMetric(e.getTarget()), e.getCurrentHop()));
	
	         nextRWMesage.setTotalDevicesChecked(e.getTotalDevicesChecked() + connectedIotDevices.size());
	         nextRWMesage.setTotalDevicesWithMetric(e.getTotalDevicesWithMetric() + this.calculateTotalDevicesWithMetric(e.getTarget()));
	      } else {
	         nextRWMesage.setTotalDevicesChecked(e.getTotalDevicesChecked());
	         nextRWMesage.setTotalDevicesWithMetric(e.getTotalDevicesWithMetric());
	      }
	
	      queryIDCache.putEntry(e.getID(), e.getSenderID());
      }

      byte[] m = nextRWMesage.getBytes();

      if (e.getCurrentHop() + 1 < e.getHopLimit()) {
         //If the message hasn't yet hit its hop limit
         this.getTcpConnectionsCache().sendToRandom(m, e.getSenderID());
      } else {
         // no more walking...
         this.getTcpConnectionsCache().sendSingle(e.getOriginatorId(), m);
      }
   }

   private void handle(GossipData e) {
      boolean updated = false;
      logger.info("Received gossip data from: " + e.getSenderID());
      for (Map.Entry<String, Integer> device : e.getDevices().entrySet()) {
         if (gossipCache.putEntryAppend(UUID.nameUUIDFromBytes(device.getKey().getBytes()), device.getKey(), device.getValue()+1, e.getSenderID()))
            updated = true;
      }
      if (updated) {
         byte[] m = new GossipData(this.getIdentity(), gossipCache.getValueLists()).getBytes();
         this.getTcpConnectionsCache().sendAll(m, e.getSenderID());
      }
   }
   
   private void handle(GossipEntries e) {
	   boolean updated = false;
	   logger.info("Received gossip entries from: " + e.getSenderID());
	   for (Entry device : e.getDevices()) {
		   updated = gossipEntries.putEntryWithProbability(UUID.nameUUIDFromBytes((device.value.getIdentityKey()+device.keyName).getBytes()), device.value, device.keyName, 0.01);
	   }
	   if (updated) {
	      byte[] m = new GossipEntries(this.getIdentity(), gossipEntries.getLocations()).getBytes();
	      this.getTcpConnectionsCache().sendAll(m, e.getSenderID());
	   }
   }

   private void handle(GossipQuery e) {
      GossipQuery nextGossipMessage = new GossipQuery(e.getID(), this.getIdentity(), e.getOriginatorId(), e.getTarget(), e.getCurrentHop() + 1, e.getHopLimit(), 0);
      nextGossipMessage.setTotalDevicesChecked(e.getTotalDevicesChecked());
      nextGossipMessage.setTotalDevicesWithMetric(e.getTotalDevicesWithMetric());
      byte[] m = nextGossipMessage.getBytes();

      synchronized(queryIDCache) {
	      if (queryIDCache.containsEntry(e.getID())) {
	         //We've already processed this query - ignore it
	         queryIDCache.putEntry(e.getID(), e.getSenderID());
	
	         this.getTcpConnectionsCache().sendSingle(e.getOriginatorId(), m);
	
	         return;
	      }
	
	      queryIDCache.putEntry(e.getID(), e.getSenderID());
      }
      logger.info("Received gossip query with ID: " + e.getID());
      //Check if queried data is here - if so, log appropriately
      metrics.put(e.getID(), new Metric(this.calculateTotalDevicesWithMetric(e.getTarget()), e.getCurrentHop()));

      // Only need to send a message that includes the current stats to one of my "gossip neighbors"
      GossipQuery nextGossipMessage2 = new GossipQuery(e.getID(), this.getIdentity(), e.getOriginatorId(), e.getTarget(), e.getCurrentHop() + 1, e.getHopLimit(), 0);
      nextGossipMessage2.setTotalDevicesChecked(e.getTotalDevicesChecked() + connectedIotDevices.size());
      nextGossipMessage2.setTotalDevicesWithMetric(e.getTotalDevicesWithMetric() + this.calculateTotalDevicesWithMetric(e.getTarget()));
      byte[] m2 = nextGossipMessage2.getBytes();
      
      nextGossipMessage.setTotalDevicesChecked(0);
      nextGossipMessage.setTotalDevicesWithMetric(0);
      m = nextGossipMessage.getBytes();

      int index = 0;
      if (e.getCurrentHop() + 1 < e.getHopLimit()) {
         //If the message hasn't yet hit its hop limit
         if(e.getGossipType() == 0) {
	         if (gossipCache.containsEntry(UUID.nameUUIDFromBytes(e.getTarget().getBytes()))) {
	            for (Map.Entry<Identity, Integer> entry : gossipCache.getEntry(UUID.nameUUIDFromBytes(e.getTarget().getBytes())).valueList.entrySet()) {
	               if (entry.getKey() != e.getSenderID() && entry.getKey() != this.getIdentity() && entry.getValue() < e.getHopLimit()-e.getCurrentHop())
	                  if(index == 0) {
                         this.getTcpConnectionsCache().sendSingle(entry.getKey(), m2);
                      } else {
                         this.getTcpConnectionsCache().sendSingle(entry.getKey(), m);
                      }
	                  index++;
	            }
	         }
         } else if(e.getGossipType() == 1) {
        	 for(cs555.chiba.util.LRUCache.Entry entry : gossipEntries.getHashmap().values()) {
        		 if(entry.keyName.contentEquals(e.getTarget()) && entry.value != this.getIdentity()) {
                    if(index == 0) {
                       this.getTcpConnectionsCache().sendSingle(entry.value, m2);
                    } else {
                       this.getTcpConnectionsCache().sendSingle(entry.value, m);
                    }
                    index++;
        		 }
        	 }
         }
      } else {
         this.getTcpConnectionsCache().sendSingle(e.getOriginatorId(), m);
      }
   }

   private void handle(InitiateConnectionsMessage message) {
	   IotTransformer trans = new IotTransformer(message.getDeviceString());
	   this.connectedIotDevices = trans.getConnectedIotDevices();
	   for(String d : getAllMetricNames()) {
	    	  gossipCache.putEntryAppend(UUID.nameUUIDFromBytes(d.getBytes()), d, 0, this.getIdentity());
	    	  gossipEntries.putEntryWithProbability(UUID.nameUUIDFromBytes((this.getIdentity().getIdentityKey()+d).getBytes()), this.getIdentity(), d, 0.01);
	  }
      message.getNeighbors().forEach(identity -> {
         this.getTcpConnectionsCache().addConnection(identity, this.getEventFactory());
      });
   }

   public List<IotDevice> getConnectedIotDevices() {
      return this.connectedIotDevices;
   }
   
   public LRUCache getGossipData() {
	   return this.gossipCache;
   }
   
   public LRUCache getGossipEntries() {
	   return this.gossipEntries;
   }
   
   public HashMap<UUID,Metric> getMetrics(){
	   return metrics;
   }

   private void handle(IntroductionMessage message) {
      Socket generatedSocket = message.getSocket();
      Identity wrongIdentity = Identity.builder().withSocketAddress(generatedSocket.getRemoteSocketAddress()).build();

      this.getTcpConnectionsCache().correctIdentity(wrongIdentity, message.getIdentity());
      
      byte[] m = new GossipData(this.getIdentity(), gossipCache.getValueLists()).getBytes();
      this.getTcpConnectionsCache().sendAll(m);
      m = new GossipEntries(this.getIdentity(), gossipEntries.getLocations()).getBytes();
      this.getTcpConnectionsCache().sendAll(m);
   }

   private void handle(ListPeersRequestMessage message) {
      try {
         ListPeersResponseMessage response = new ListPeersResponseMessage(ServiceNode.getThisNode().getIdentity(), this.getTcpConnectionsCache().listPeers());
         this.getTcpConnectionsCache().sendSingle(this.registryId, response.getBytes());
      }
      catch (IOException e) {
         logger.log(Level.SEVERE, "Failed to list neighboring peers!", e);
      }
   }
   /**
    * Routes messages to the correct handler
    * @param e The message that must be handled
    */
   @Override public void onEvent(Event e) {
      if (e instanceof Flood) {
         handle((Flood) e);
      }
      else if (e instanceof RandomWalk) {
         handle((RandomWalk) e);
      }
      else if (e instanceof GossipData) {
         handle((GossipData) e);
      }
      else if (e instanceof GossipEntries) {
          handle((GossipEntries) e);
       }
      else if (e instanceof GossipQuery) {
         handle((GossipQuery) e);
      }
      else if (e instanceof InitiateConnectionsMessage) {
         handle((InitiateConnectionsMessage) e);
      }
      else if (e instanceof IntroductionMessage) {
         handle((IntroductionMessage) e);
      }
      else if (e instanceof ShutdownMessage) {
         ServiceNode.getThisNode().shutdown();
      }
      else if (e instanceof ListPeersRequestMessage) {
         handle((ListPeersRequestMessage) e);
      }
      else {
         logger.severe("Cannot handle message [" + e.getClass().getSimpleName() + "]");
      }
   }

   /**
    * Creates a new Peer, then idles until exit
    * @param args The IP address of the registry and the port of the registry
    */
   public static void main(String[] args) {
      {
         try {
            Peer node = parseArguments(args);
            startup(0, node, PeerCommands.getPeerCommands(node), "Peer->");
         }
         catch (Exception e) {
            logger.log(Level.SEVERE, "Startup failed", e);
         }
      }
   }

   private static Peer parseArguments(String[] args) throws IOException {

      if (!Utilities.checkArgCount(4, args)) {
         throw new IllegalArgumentException("Peer Node requires 3 arguments:  registry-host registry-port iot-count cache-size");
      }

      String registryHots = args[0];
      int registryPort = Utilities.parsePort(args[1]);
      int iotCount = Integer.parseInt(args[2]);
      int cacheSize = Integer.parseInt(args[3]);

      return new Peer(registryHots, registryPort, iotCount, cacheSize);
   }
}
