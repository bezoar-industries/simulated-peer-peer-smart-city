package cs555.chiba.registry;

import cs555.chiba.service.Identity;
import cs555.chiba.service.ServiceNode;
import cs555.chiba.util.Utilities;
import cs555.chiba.wireformats.RegisterMessage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * @author mmuller
 *
 * Just a collection of registered peers
 */
public class RegisteredPeers {

   private static final Logger logger = Logger.getLogger(RegisteredPeers.class.getName());

   private Map<Identity, String> registry = new ConcurrentHashMap<>(); // registered peers

   /**
    * Register the node with the system.
    */
   public void register(RegisterMessage message) {
      if (ServiceNode.getThisNode().getIdentity().getIdentityKey().equals(message.getIdentity().getIdentityKey())) {
         throw new IllegalArgumentException("You can't register the registry node! [" + message.getIdentity().getIdentityKey() + "]");
      }

      Identity ident = message.getIdentity();
      String deviceCount = this.registry.get(ident);

      if (deviceCount != null) {
         logger.severe("The requesting peer is already registered [" + ident.getIdentityKey() + "]'");
      }
      else {
         this.registry.put(ident, message.getDeviceList());
         logger.info("The node [" + ident.getIdentityKey() + "] has been registered.");
      }
   }

   public List<Identity> listRegisteredPeers() {
      // changes to keySet are reflected in the map, so let's make a copy
      return Utilities.copy(this.registry.keySet());
   }
}
