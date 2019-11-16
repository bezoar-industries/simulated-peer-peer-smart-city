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
import cs555.chiba.wireformats.GossipQuery;
import cs555.chiba.wireformats.RandomWalk;
import cs555.chiba.wireformats.RegisterMessage;
import cs555.chiba.wireformats.SampleMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Peer extends ServiceNode {

   private static final Logger logger = Logger.getLogger(Peer.class.getName());

   private UUID id;
   private Identity registryId;
   private LRUCache queryIDCache;
   private LRUCache gossipCache;
   private HashMap<UUID, Metric> metrics;
   private List<IotDevice> connectedIotDevices;

   public Peer(String registryHost, int registryPort, int numberOfIoTDevices) throws IOException {
      super();
      this.id = UUID.randomUUID();
      this.registryId = Identity.builder().withHost(registryHost).withPort(registryPort).build();
      this.createIotNetwork(numberOfIoTDevices);

      this.queryIDCache = new LRUCache(1000);
      this.gossipCache = new LRUCache(1000);
      this.metrics = new HashMap<>();
   }

   @Override protected void specialStartUp() {
      register();
   }

   private void register() {
      try {
         logger.info("Registering: [" + this.getIdentity().getIdentityName() + "] \n");
         RegisterMessage message = new RegisterMessage(this.getIdentity(), id);
         this.getTcpConnectionsCache().send(this.registryId, message.getBytes());
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
    * A handler for SampleMessages
    * @param e The SampleMessage
    */
   public void SampleMessage(SampleMessage e) {
      logger.info("Recieved sample message with num: " + e.getNum());
   }

   /**
    * A handler for Flood messages
    * @param e The Flood message
    */
   public void Flood(Flood e) {
      if (queryIDCache.containsEntry(e.getID())) {
         //We've already processed this query - ignore it
         queryIDCache.putEntry(e.getID(), e.getSenderID());
         return;
      }

      logger.info("Recieved flood message with ID: " + e.getID());
      queryIDCache.putEntry(e.getID(), e.getSenderID());
      //Check if queried data is here - if so, log appropriately
      metrics.put(e.getID(), new Metric(0, e.getCurrentHop()));

      if (e.getCurrentHop() + 1 < e.getHopLimit() || e.getHopLimit() == -1) {
         //If the message hasn't yet hit its hop limit
         byte[] m = new Flood(e.getID(), id, e.getTarget(), e.getCurrentHop() + 1, e.getHopLimit()).getBytes();
         this.getTcpConnectionsCache().sendAll(m, e.getSenderID());
      }
   }

   /**
    * A handler for RandomWalk messages
    * @param e The RandomWalk message
    */
   public void RandomWalk(RandomWalk e) {
      if (!queryIDCache.containsEntry(e.getID())) {
         //We've already processed this query - don't process it again (but still forward it)
         logger.info("Received random walk message with ID: " + e.getID());
         //Check if queried data is here - if so, log appropriately
         metrics.put(e.getID(), new Metric(0, e.getCurrentHop()));
      }
      queryIDCache.putEntry(e.getID(), e.getSenderID());

      if (e.getCurrentHop() + 1 < e.getHopLimit()) {
         //If the message hasn't yet hit its hop limit
         byte[] m = new Flood(e.getID(), id, e.getTarget(), e.getCurrentHop() + 1, e.getHopLimit()).getBytes();
         this.getTcpConnectionsCache().sendToRandom(m, e.getSenderID());
      }
   }

   private void GossipData(GossipData e) {
      boolean updated = false;
      logger.info("Received gossip query from: " + e.getSenderID());
      for (String device : e.getDevices()) {
         if (gossipCache.putEntryAppend(UUID.nameUUIDFromBytes(device.getBytes()), device, e.getSenderID()))
            updated = true;
      }
      if (updated) {
         byte[] m = new GossipData(id, gossipCache.getValueLists()).getBytes();
         this.getTcpConnectionsCache().sendAll(m, e.getSenderID());
      }
   }

   private void GossipQuery(GossipQuery e) {
      if (queryIDCache.containsEntry(e.getID())) {
         //We've already processed this query - ignore it
         queryIDCache.putEntry(e.getID(), e.getSenderID());
         return;
      }

      logger.info("Received gossip message with ID: " + e.getID());
      queryIDCache.putEntry(e.getID(), e.getSenderID());
      //Check if queried data is here - if so, log appropriately
      metrics.put(e.getID(), new Metric(0, e.getCurrentHop()));

      if (e.getCurrentHop() + 1 < e.getHopLimit()) {
         //If the message hasn't yet hit its hop limit
         byte[] m = new Flood(e.getID(), id, e.getTarget(), e.getCurrentHop() + 1, e.getHopLimit()).getBytes();
         if (gossipCache.containsEntry(UUID.nameUUIDFromBytes(e.getTarget().getBytes()))) {
            for (UUID targetID : gossipCache.getEntry(UUID.nameUUIDFromBytes(e.getTarget().getBytes())).valueList) {
               if (targetID != e.getSenderID())
                  this.getTcpConnectionsCache().send(targetID, m);
            }
         }
      }
   }

   /**
    * Routes messages to the correct handler
    * @param e The message that must be handled
    */
   @Override public void onEvent(Event e) {
      if (e instanceof SampleMessage) {
         SampleMessage((SampleMessage) e);
      }
      else if (e instanceof Flood) {
         Flood((Flood) e);
      }
      else if (e instanceof RandomWalk) {
         RandomWalk((RandomWalk) e);
      }
      else if (e instanceof GossipData) {
         GossipData((GossipData) e);
      }
      else if (e instanceof GossipQuery) {
         GossipQuery((GossipQuery) e);
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
