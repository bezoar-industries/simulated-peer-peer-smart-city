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
import cs555.chiba.util.Metric;
import cs555.chiba.util.Utilities;
import cs555.chiba.wireformats.Event;
import cs555.chiba.wireformats.Flood;
import cs555.chiba.wireformats.GossipData;
import cs555.chiba.wireformats.GossipEntries;
import cs555.chiba.wireformats.GossipQuery;
import cs555.chiba.wireformats.InitiateConnectionsMessage;
import cs555.chiba.wireformats.IntroductionMessage;
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

   public Peer(String registryHost, int registryPort, int numberOfIoTDevices) throws IOException {
      super();
      this.registryId = Identity.builder().withHost(registryHost).withPort(registryPort).build();
      this.createIotNetwork(numberOfIoTDevices);

      this.queryIDCache = new LRUCache(40);
      this.gossipCache = new LRUCache(40);
      this.gossipEntries = new LRUCache(40);
      for(IotDevice d : connectedIotDevices) {
    	  gossipCache.putEntryAppend(UUID.nameUUIDFromBytes(d.toString().getBytes()), d.toString(), 0, this.getIdentity());
      }
      this.metrics = new HashMap<>();
   }

   @Override protected void specialStartUp() {
      register();
   }

   private void register() {
      try {
         logger.info("Registering: [" + this.getIdentity().getIdentityName() + "] \n");
         RegisterMessage message = new RegisterMessage(this.getIdentity());
         this.getTcpConnectionsCache().sendSingle(this.registryId, message.getBytes());
      }
      catch (Exception e) {
         throw new IllegalStateException("Unable to communicate with Registry.  Double check your host and registry before running again.", e);
      }
   }

   private void createIotNetwork(int numberOfIoTDevices) {
      this.connectedIotDevices = new LinkedList<>();
      if (numberOfIoTDevices == 0) {
         // if no number of IoT devices defined, then generate a random number between 3 and 30 devices
         // Upper bounds of 27 and then adding 3 to ensure the range above is followed
         Random random = new Random();
         numberOfIoTDevices = random.nextInt(27) + 3;
      }

      logger.info("number of devices: " + numberOfIoTDevices);

      // Add devices to our array list
      for (int i = 0; i < numberOfIoTDevices; i++) {

         // Change this int when adding a new IoT device
         int numberOfPossibleIotDevices = 19;
         Random random = new Random();
         int possibleDevicesRandomIndex = random.nextInt(numberOfPossibleIotDevices);
         switch (possibleDevicesRandomIndex) {
            case 0:
               this.connectedIotDevices.add(new Thermometer());
               break;
            case 1:
               this.connectedIotDevices.add(new Thermostat());
               break;
            case 2:
               this.connectedIotDevices.add(new DoorLock());
               break;
            case 3:
               this.connectedIotDevices.add(new Outlet());
               break;
            case 4:
               this.connectedIotDevices.add(new AirPollutionMonitor());
               break;
            case 5:
               this.connectedIotDevices.add(new AirVent());
               break;
            case 6:
               this.connectedIotDevices.add(new DoorSensor());
               break;
            case 7:
               this.connectedIotDevices.add(new Dryer());
               break;
            case 8:
               this.connectedIotDevices.add(new LightSwitch());
               break;
            case 9:
               this.connectedIotDevices.add(new Microwave());
               break;
            case 10:
               this.connectedIotDevices.add(new Refrigerator());
               break;
            case 11:
               this.connectedIotDevices.add(new TV());
               break;
            case 12:
               this.connectedIotDevices.add(new WashMachine());
               break;
            case 13:
               this.connectedIotDevices.add(new Watch());
               break;
            case 14:
               this.connectedIotDevices.add(new WaterLeakSensor());
               break;
            case 15:
               this.connectedIotDevices.add(new WindowSensor());
               break;
            case 16:
               this.connectedIotDevices.add(new Clock());
               break;
            case 17:
               this.connectedIotDevices.add(new StreetLight());
               break;
            case 18:
               this.connectedIotDevices.add(new PowerMeter());
               break;
         }
      }
      logger.info(this.connectedIotDevices.toString());
      logger.info("Number of actual devices: " + this.connectedIotDevices.size());
   }

   private Integer calculateTotalDevicesWithMetric(String metricName) {
      int devicesWithMetric = 0;
      for (IotDevice device : this.connectedIotDevices) {
         devicesWithMetric += device.getMetric(metricName);
      }
      return devicesWithMetric;
   }

   /**
    * A handler for Flood messages
    * @param e The Flood message
    */
   public void handle(Flood e) {
      if (queryIDCache.containsEntry(e.getID())) {
         //We've already processed this query - ignore it
         queryIDCache.putEntry(e.getID(), e.getSenderID());

         Flood nextFloodMesage = new Flood(e.getID(), this.getIdentity(), e.getOriginatorId(), e.getTarget(), e.getCurrentHop() + 1, e.getHopLimit());
         nextFloodMesage.setTotalDevicesChecked(e.getTotalDevicesChecked() + connectedIotDevices.size());
         nextFloodMesage.setTotalDevicesWithMetric(e.getTotalDevicesWithMetric() + this.calculateTotalDevicesWithMetric(e.getTarget()));
         byte[] m = nextFloodMesage.getBytes();

         this.getTcpConnectionsCache().sendSingle(e.getOriginatorId(), m);

         return;
      }

      logger.info("Recieved flood message with ID: " + e.getID());
      queryIDCache.putEntry(e.getID(), e.getSenderID());
      //Check if queried data is here - if so, log appropriately
      metrics.put(e.getID(), new Metric(0, e.getCurrentHop()));

      if (e.getCurrentHop() + 1 < e.getHopLimit() || e.getHopLimit() == -1) {
         //If the message hasn't yet hit its hop limit

         Flood nextFloodMesage = new Flood(e.getID(), this.getIdentity(), e.getOriginatorId(), e.getTarget(), e.getCurrentHop() + 1, e.getHopLimit());
         nextFloodMesage.setTotalDevicesChecked(e.getTotalDevicesChecked() + connectedIotDevices.size());
         nextFloodMesage.setTotalDevicesWithMetric(e.getTotalDevicesWithMetric() + this.calculateTotalDevicesWithMetric(e.getTarget()));
         byte[] m = nextFloodMesage.getBytes();
         Identity randomNode = this.getTcpConnectionsCache().getRandomSender().getIdentity();
         this.getTcpConnectionsCache().sendSingle(randomNode, m);

         List<Identity> nodesToExclude = new ArrayList<>();
         nodesToExclude.add(randomNode);
         nodesToExclude.add(e.getSenderID());

         nextFloodMesage = new Flood(e.getID(), this.getIdentity(), e.getOriginatorId(), e.getTarget(), e.getCurrentHop() + 1, e.getHopLimit());
         nextFloodMesage.setTotalDevicesChecked(e.getTotalDevicesChecked());
         nextFloodMesage.setTotalDevicesWithMetric(e.getTotalDevicesWithMetric());

         this.getTcpConnectionsCache().sendAll(m, nodesToExclude);
      } else {
         // No more hops for the flooding to take
         Flood nextFloodMesage = new Flood(e.getID(), this.getIdentity(), e.getOriginatorId(), e.getTarget(), e.getCurrentHop() + 1, e.getHopLimit());
         nextFloodMesage.setTotalDevicesChecked(e.getTotalDevicesChecked() + connectedIotDevices.size());
         nextFloodMesage.setTotalDevicesWithMetric(e.getTotalDevicesWithMetric() + this.calculateTotalDevicesWithMetric(e.getTarget()));
         byte[] m = nextFloodMesage.getBytes();

         this.getTcpConnectionsCache().sendSingle(e.getOriginatorId(), m);

      }
   }

   /**
    * A handler for RandomWalk messages
    * @param e The RandomWalk message
    */
   public void handle(RandomWalk e) {
      if (!queryIDCache.containsEntry(e.getID())) {
         //We've already processed this query - don't process it again (but still forward it)
         logger.info("Received random walk message with ID: " + e.getID());
         //Check if queried data is here - if so, log appropriately
         metrics.put(e.getID(), new Metric(0, e.getCurrentHop()));
      }
      queryIDCache.putEntry(e.getID(), e.getSenderID());

      if (e.getCurrentHop() + 1 < e.getHopLimit()) {
         //If the message hasn't yet hit its hop limit
         RandomWalk nextRWMesage = new RandomWalk(e.getID(), this.getIdentity(), e.getOriginatorId(), e.getTarget(), e.getCurrentHop() + 1, e.getHopLimit());
         nextRWMesage.setTotalDevicesChecked(e.getTotalDevicesChecked() + connectedIotDevices.size());
         nextRWMesage.setTotalDevicesWithMetric(e.getTotalDevicesWithMetric() + this.calculateTotalDevicesWithMetric(e.getTarget()));
         byte[] m = nextRWMesage.getBytes();

         this.getTcpConnectionsCache().sendToRandom(m, e.getSenderID());
      } else {
         // no more walking...
         RandomWalk nextRWMesage = new RandomWalk(e.getID(), this.getIdentity(), e.getOriginatorId(), e.getTarget(), e.getCurrentHop() + 1, e.getHopLimit());
         nextRWMesage.setTotalDevicesChecked(e.getTotalDevicesChecked() + connectedIotDevices.size());
         nextRWMesage.setTotalDevicesWithMetric(e.getTotalDevicesWithMetric() + this.calculateTotalDevicesWithMetric(e.getTarget()));
         byte[] m = nextRWMesage.getBytes();

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
	   for (Map.Entry<Identity,String> device : e.getDevices().entrySet()) {
	      if (gossipEntries.putEntryWithProbability(UUID.nameUUIDFromBytes((device.getKey().getIdentityKey()+device.getValue()).getBytes()), device.getKey(), device.getValue(), 0.01))
	         updated = true;
	   }
	   if (updated) {
	      byte[] m = new GossipData(this.getIdentity(), gossipCache.getValueLists()).getBytes();
	      this.getTcpConnectionsCache().sendAll(m, e.getSenderID());
	   }
   }

   private void handle(GossipQuery e) {
      if (queryIDCache.containsEntry(e.getID())) {
         //We've already processed this query - ignore it
         queryIDCache.putEntry(e.getID(), e.getSenderID());

         GossipQuery nextGossipMesage = new GossipQuery(e.getID(), this.getIdentity(), e.getOriginatorId(), e
                 .getTarget(), e.getCurrentHop() + 1, e.getHopLimit(), 0);
         nextGossipMesage.setTotalDevicesChecked(e.getTotalDevicesChecked() + connectedIotDevices.size());
         nextGossipMesage.setTotalDevicesWithMetric(e.getTotalDevicesWithMetric() + this.calculateTotalDevicesWithMetric(e.getTarget()));
         byte[] m = nextGossipMesage.getBytes();

         this.getTcpConnectionsCache().sendSingle(e.getOriginatorId(), m);

         return;
      }

      logger.info("Received gossip query with ID: " + e.getID());
      queryIDCache.putEntry(e.getID(), e.getSenderID());
      //Check if queried data is here - if so, log appropriately
      metrics.put(e.getID(), new Metric(0, e.getCurrentHop()));

      if (e.getCurrentHop() + 1 < e.getHopLimit()) {
         //If the message hasn't yet hit its hop limit
         GossipQuery nextGossipMesage = new GossipQuery(e.getID(), this.getIdentity(), e.getOriginatorId(), e.getTarget(), e.getCurrentHop() + 1, e.getHopLimit(), e.getGossipType());
         nextGossipMesage.setTotalDevicesChecked(e.getTotalDevicesChecked() + connectedIotDevices.size());
         nextGossipMesage.setTotalDevicesWithMetric(e.getTotalDevicesWithMetric() + this.calculateTotalDevicesWithMetric(e.getTarget()));
         byte[] m = nextGossipMesage.getBytes();
         if(e.getGossipType() == 0) {
	         if (gossipCache.containsEntry(UUID.nameUUIDFromBytes(e.getTarget().getBytes()))) {
	            for (Map.Entry<Identity, Integer> entry : gossipCache.getEntry(UUID.nameUUIDFromBytes(e.getTarget().getBytes())).valueList.entrySet()) {
	               if (entry.getKey() != e.getSenderID() && entry.getKey() != this.getIdentity() && entry.getValue() < e.getHopLimit()-e.getCurrentHop())
	                  this.getTcpConnectionsCache().send(entry.getKey(), m);
	            }
	         }
         } else if(e.getGossipType() == 1) {
        	 for(cs555.chiba.util.LRUCache.Entry entry : gossipEntries.getHashmap().values()) {
        		 if(entry.keyName.contentEquals(e.getTarget())) {
        			 this.getTcpConnectionsCache().sendSingle(entry.value, m);
        		 }
        	 }
         }
      } else {
         GossipQuery nextGossipMesage = new GossipQuery(e.getID(), this.getIdentity(), e.getOriginatorId(), e
                 .getTarget(), e.getCurrentHop() + 1, e.getHopLimit(), 0);
         nextGossipMesage.setTotalDevicesChecked(e.getTotalDevicesChecked() + connectedIotDevices.size());
         nextGossipMesage.setTotalDevicesWithMetric(e.getTotalDevicesWithMetric() + this.calculateTotalDevicesWithMetric(e.getTarget()));
         byte[] m = nextGossipMesage.getBytes();

         this.getTcpConnectionsCache().sendSingle(e.getOriginatorId(), m);
      }
   }

   private void handle(InitiateConnectionsMessage message) {
      message.getNeighbors().forEach(identity -> {
         this.getTcpConnectionsCache().addConnection(identity, this.getEventFactory());
      });
   }

   private void handle(IntroductionMessage message) {
      Socket generatedSocket = message.getSocket();
      Identity wrongIdent = Identity.builder().withSocketAddress(generatedSocket.getRemoteSocketAddress()).build();

      this.getTcpConnectionsCache().correctIdentity(wrongIdent, message.getIdentity());
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

      if (!Utilities.checkArgCount(3, args)) {
         throw new IllegalArgumentException("Peer Node requires 3 arguments:  registry-host registry-port iot-count");
      }

      String registryHots = args[0];
      int registryPort = Utilities.parsePort(args[1]);
      int iotCount = Integer.parseInt(args[2]);

      return new Peer(registryHots, registryPort, iotCount);
   }
}
