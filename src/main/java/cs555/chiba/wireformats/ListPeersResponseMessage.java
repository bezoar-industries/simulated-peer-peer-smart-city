package cs555.chiba.wireformats;

import cs555.chiba.service.Identity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class ListPeersResponseMessage extends Message {

   private Identity peer;
   private List<Identity> neighbors;

   public ListPeersResponseMessage(Identity peer, List<Identity> neighbors) {
      super(Protocol.LIST_PEERS_RESPONSE);
      this.peer = peer;
      this.neighbors = neighbors;
   }

   public ListPeersResponseMessage(byte[] message) throws IOException {
      super(Protocol.LIST_PEERS_RESPONSE, message);
   }

   @Override void parse(DataInputStream input) throws IOException {
      this.peer = Identity.builder().withIdentityKey(readString(input)).build();
      this.neighbors = readListOfIdentities(input);
   }

   @Override void spool(DataOutputStream output) throws IOException {
      sendString(this.peer.getIdentityKey(), output);
      sendListOfIdentities(this.neighbors, output);
   }

   public List<Identity> getNeighbors() {
      return neighbors;
   }

   public Identity getPeer() {
      return peer;
   }

   @Override public boolean equals(Object o) {
      if (this == o)
         return true;
      if (o == null || getClass() != o.getClass())
         return false;
      if (!super.equals(o))
         return false;
      ListPeersResponseMessage that = (ListPeersResponseMessage) o;
      return Objects.equals(peer, that.peer) && Objects.equals(neighbors, that.neighbors);
   }

   @Override public int hashCode() {
      return Objects.hash(super.hashCode(), peer, neighbors);
   }

   @Override public String toString() {
      return "ListPeersResponseMessage{" + "peer=" + peer + ", neighbors=" + neighbors + "} " + super.toString();
   }
}
