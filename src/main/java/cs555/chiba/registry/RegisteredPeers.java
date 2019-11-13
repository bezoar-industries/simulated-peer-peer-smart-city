package cs555.chiba.registry;

import cs555.chiba.service.Identity;
import cs555.chiba.service.ServiceNode;
import cs555.chiba.util.Utilities;
import cs555.chiba.wireformats.RegisterMessage;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * @author mmuller
 *
 * Just a collection of registered peers
 */
public class RegisteredPeers {

   private static final Logger logger = Logger.getLogger(RegisteredPeers.class.getName());

   private Map<UUID, Identity> registry = new ConcurrentHashMap<>(); // registered peers

   /**
    * Register the node with the system.
    */
   public void register(RegisterMessage message) {
      if (ServiceNode.getThisNode().getIdentity().getIdentityKey().equals(message.getIdentity().getIdentityKey())) {
         throw new IllegalArgumentException("You can't register the registry node! [" + message.getIdentity().getIdentityKey() + "]");
      }

      Identity ident = message.getIdentity();
      Identity peer = this.registry.get(message.getUuid());

      if (peer != null) {
         logger.severe("The requesting peer is already registered [" + ident.getIdentityKey() + "]'");
      }
      else {
         this.registry.put(message.getUuid(), ident);
         logger.info("The node [" + ident.getIdentityKey() + "] has been registered.");
      }
   }

   public List<Identity> listRegisteredPeers() {
      // changes to keySet are reflected in the map, so let's make a copy
      return Utilities.copy(this.registry.values());
   }

   public Identity getPeer(UUID peer) {
      return this.registry.get(peer);
   }
}
