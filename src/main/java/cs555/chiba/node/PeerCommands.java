package cs555.chiba.node;

import cs555.chiba.service.Commands;

import java.util.logging.Logger;

/**
 * @author mmuller
 *
 * Available Peer Node commands
 */
class PeerCommands {

   private static final Logger logger = Logger.getLogger(PeerCommands.class.getName());

   static Commands getPeerCommands(Peer peer) {
      Commands.Builder builder = Commands.builder();
      builder.registerCommand("listpeers", args -> { // list all the registered nodes
         logger.info(listPeers(peer));
         return null;
      });

      builder.registerCommand("listdevices", args -> { // list all the registered nodes
         logger.info(listDevices(peer));
         return null;
      });

      return builder.build();
   }

   /**
    * Who are our neighbors
    */
   private static String listPeers(Peer peer) {
      StringBuffer out = new StringBuffer("Neighboring Peers: \n");
      peer.getTcpConnectionsCache().listConnections().forEach(ident -> {
         out.append(ident).append("\n");
      });

      return out.toString();
   }

   /**
    * Who are our neighbors
    */
   private static String listDevices(Peer peer) {
      StringBuffer out = new StringBuffer("IoT Devices: \n");
      peer.getConnectedIotDevices().forEach(device -> {
         out.append(device).append("\n");
      });

      return out.toString();
   }
}
