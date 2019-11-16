package cs555.chiba.registry;

import cs555.chiba.overlay.network.NetworkMap;
import cs555.chiba.service.Identity;
import cs555.chiba.service.ServiceNode;
import cs555.chiba.util.Utilities;
import cs555.chiba.wireformats.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RegistryNode extends ServiceNode {

   private static final Logger logger = Logger.getLogger(RegistryNode.class.getName());

   public static void main(String[] args) {
      try {
         RegistryNode node = parseArguments(args);
         startup(node.getIdentity().getPort(), node, RegistryCommands.getRegistryCommands(node), "Registry->");
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
      Identity ident = Identity.builder().withHost(InetAddress.getLocalHost().getHostAddress()).withPort(port).build();

      logger.info("Starting up as [" + ident.getIdentityKey() + "]");
      return new RegistryNode(ident);
   }

   private final RegisteredPeers registry = new RegisteredPeers();
   private NetworkMap networkMap;

   private ConcurrentHashMap<UUID, ResultMetrics> requests = new ConcurrentHashMap<>();

   public RegistryNode(Identity ident) {
      super();
      setIdentity(ident);
   }

   @Override public void onEvent(Event event) {
      logger.info("We got a message! [" + event + "]");
      {
         if (event instanceof RegisterMessage) {
            handle((RegisterMessage) event);
         } else if (event instanceof RandomWalk) {
            RandomWalk randomWalkMessage = (RandomWalk)event;
            ResultMetrics resultMetrics;
            if(requests.containsKey(randomWalkMessage.getID())) {
               resultMetrics = requests.get(randomWalkMessage.getID());
            } else {
               resultMetrics = new ResultMetrics(randomWalkMessage.getID(), 0, 0);
            }

            resultMetrics.addTotalNumberOfDevices(randomWalkMessage.getTotalDevicesWithMetric());
            resultMetrics.addTotalNumberOfHops(randomWalkMessage.getCurrentHop());
            resultMetrics.setTimeOfLastReceivedResultMessage();

            requests.merge(randomWalkMessage.getID(), resultMetrics, ResultMetrics::merge);
         } else if (event instanceof GossipQuery) {
            GossipQuery gossipQueryMessage = (GossipQuery)event;
            ResultMetrics resultMetrics;
            if(requests.containsKey(gossipQueryMessage.getID())) {
               resultMetrics = requests.get(gossipQueryMessage.getID());
            } else {
               resultMetrics = new ResultMetrics(gossipQueryMessage.getID(), 0, 0);
            }

            resultMetrics.addTotalNumberOfDevices(gossipQueryMessage.getTotalDevicesWithMetric());
            resultMetrics.addTotalNumberOfHops(gossipQueryMessage.getCurrentHop());
            resultMetrics.setTimeOfLastReceivedResultMessage();

            requests.merge(gossipQueryMessage.getID(), resultMetrics, ResultMetrics::merge);
         } else if (event instanceof Flood) {
            Flood floodMessage = (Flood)event;

            ResultMetrics resultMetrics;
            if(requests.containsKey(floodMessage.getID())) {
               resultMetrics = requests.get(floodMessage.getID());
            } else {
               resultMetrics = new ResultMetrics(floodMessage.getID(), 0, 0);
            }

            resultMetrics.addTotalNumberOfDevices(floodMessage.getTotalDevicesWithMetric());
            resultMetrics.addTotalNumberOfHops(floodMessage.getCurrentHop());
            resultMetrics.setTimeOfLastReceivedResultMessage();

            requests.merge(floodMessage.getID(), resultMetrics, ResultMetrics::merge);
         } else {
            logger.severe("Cannot handle message [" + event.getClass().getSimpleName() + "]");
         }
      }
   }

   public void addRequest(UUID requestId) {
      ResultMetrics resultMetrics = new ResultMetrics(requestId, 0, 0);
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
}
