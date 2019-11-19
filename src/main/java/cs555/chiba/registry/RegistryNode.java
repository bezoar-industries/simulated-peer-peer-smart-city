package cs555.chiba.registry;

import cs555.chiba.overlay.network.NetworkMap;
import cs555.chiba.service.ServiceNode;
import cs555.chiba.util.Utilities;
import cs555.chiba.wireformats.Event;
import cs555.chiba.wireformats.Flood;
import cs555.chiba.wireformats.GossipQuery;
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

   private ConcurrentHashMap<UUID, ResultMetrics> requests = new ConcurrentHashMap<>();

   public RegistryNode(int port) {
      super();
      this.port = port;
   }

   @Override public void onEvent(Event event) {
      logger.info("We got a message! [" + event + "]");
      {
         if (event instanceof RegisterMessage) {
            handle((RegisterMessage) event);
         }
         else if (event instanceof RandomWalk) {
            RandomWalk randomWalkMessage = (RandomWalk) event;
            ResultMetrics resultMetrics;
            if (requests.containsKey(randomWalkMessage.getID())) {
               resultMetrics = requests.get(randomWalkMessage.getID());
            }
            else {
               resultMetrics = new ResultMetrics(randomWalkMessage.getID(), 0, 0, 0);
            }

            resultMetrics.addTotalNumberOfDevices(randomWalkMessage.getTotalDevicesChecked());
            resultMetrics.addTotalNumberOfHops(randomWalkMessage.getCurrentHop());
            resultMetrics.setTimeOfLastReceivedResultMessage();
            resultMetrics.addTotalNumberOfDevicesWithMetric(randomWalkMessage.getTotalDevicesWithMetric());

            requests.merge(randomWalkMessage.getID(), resultMetrics, ResultMetrics::merge);
         }
         else if (event instanceof GossipQuery) {
            GossipQuery gossipQueryMessage = (GossipQuery) event;
            ResultMetrics resultMetrics;
            if (requests.containsKey(gossipQueryMessage.getID())) {
               resultMetrics = requests.get(gossipQueryMessage.getID());
            }
            else {
               resultMetrics = new ResultMetrics(gossipQueryMessage.getID(), 0, 0, 0);
            }

            resultMetrics.addTotalNumberOfDevices(gossipQueryMessage.getTotalDevicesChecked());
            resultMetrics.addTotalNumberOfHops(gossipQueryMessage.getCurrentHop());
            resultMetrics.setTimeOfLastReceivedResultMessage();
            resultMetrics.addTotalNumberOfDevicesWithMetric(gossipQueryMessage.getTotalDevicesWithMetric());

            requests.merge(gossipQueryMessage.getID(), resultMetrics, ResultMetrics::merge);
         }
         else if (event instanceof Flood) {
            Flood floodMessage = (Flood) event;

            ResultMetrics resultMetrics;
            if (requests.containsKey(floodMessage.getID())) {
               resultMetrics = requests.get(floodMessage.getID());
            }
            else {
               resultMetrics = new ResultMetrics(floodMessage.getID(), 0, 0, 0);
            }

            resultMetrics.addTotalNumberOfDevices(floodMessage.getTotalDevicesChecked());
            resultMetrics.addTotalNumberOfHops(floodMessage.getCurrentHop());
            resultMetrics.addTotalNumberOfDevicesWithMetric(floodMessage.getTotalDevicesWithMetric());
            resultMetrics.setTimeOfLastReceivedResultMessage();

            requests.merge(floodMessage.getID(), resultMetrics, ResultMetrics::merge);
         }
         else {
            logger.severe("Cannot handle message [" + event.getClass().getSimpleName() + "]");
         }
      }
   }

   public void addRequest(UUID requestId) {
      ResultMetrics resultMetrics = new ResultMetrics(requestId, 0, 0, 0);
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
}
