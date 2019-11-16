package cs555.chiba.wireformats;

import cs555.chiba.service.Identity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class InitiateConnectionsMessage extends Message {

   private List<Identity> neighbors;

   public InitiateConnectionsMessage(List<Identity> neighbors) {
      super(Protocol.INITIATE_CONNECTIONS);
      this.neighbors = neighbors;
   }

   public InitiateConnectionsMessage(byte[] message) throws IOException {
      super(Protocol.INITIATE_CONNECTIONS, message);
   }

   @Override void parse(DataInputStream input) throws IOException {
      this.neighbors = readListOfIdentities(input);
   }

   @Override void spool(DataOutputStream output) throws IOException {
      sendListOfIdentities(this.neighbors, output);
   }

   public List<Identity> getNeighbors() {
      return neighbors;
   }

   @Override public String toString() {
      return "InitiateConnectionsMessage{" + "neighbors=" + neighbors + "} " + super.toString();
   }
}
