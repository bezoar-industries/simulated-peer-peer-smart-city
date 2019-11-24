package cs555.chiba.overlay.network;

import cs555.chiba.service.Identity;
import cs555.chiba.wireformats.ListPeersResponseMessage;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class NetworkMapCheck {

   private static final Logger logger = Logger.getLogger(NetworkMapCheck.class.getName());

   private NetworkMap map;
   private int checkOverlayCount = 0;

   public NetworkMapCheck(NetworkMap map) {
      this.map = map;
   }

   public synchronized void handle(ListPeersResponseMessage message) {
      Vertex vert = this.map.findVertex(message.getPeer());

      if (vert == null) {
         logger.severe("Unable to find peer in Overlay!!! [" + message.getPeer().getIdentityKey() + "]");
      }
      else {
         List<String> expected = vert.getUnfilteredConnectionList().stream().map(Identity::getIdentityKey).sorted().collect(Collectors.toList());
         List<String> actual = message.getNeighbors().stream().map(Identity::getIdentityKey).sorted().collect(Collectors.toList());

         if (!expected.equals(actual)) {
            logger.severe("Peer is not connected to the right neighbors!! [" + message.getPeer().getIdentityKey() + "]  Expected [" + expected + "]  Actual [" + actual + "]");
         }
         else {
            this.checkOverlayCount++;
            logger.info("[" + this.checkOverlayCount + "] out of [" + this.map.size() + "] Peers have successfully checked in");
         }
      }
   }
}
