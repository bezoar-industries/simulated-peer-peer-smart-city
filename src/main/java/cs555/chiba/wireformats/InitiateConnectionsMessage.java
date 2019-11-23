package cs555.chiba.wireformats;

import cs555.chiba.service.Identity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class InitiateConnectionsMessage extends Message {

   private List<Identity> neighbors;
   private String deviceString;

   public InitiateConnectionsMessage(List<Identity> neighbors, String deviceString) {
      super(Protocol.INITIATE_CONNECTIONS);
      this.neighbors = neighbors;
      this.deviceString = deviceString;
   }

   public InitiateConnectionsMessage(byte[] message) throws IOException {
      super(Protocol.INITIATE_CONNECTIONS, message);
   }

   @Override void parse(DataInputStream input) throws IOException {
      this.deviceString = readString(input);
      this.neighbors = readListOfIdentities(input);
   }

   @Override void spool(DataOutputStream output) throws IOException {
      sendString(this.deviceString, output);
      sendListOfIdentities(this.neighbors, output);
   }

   public List<Identity> getNeighbors() {
      return neighbors;
   }

   public String getDeviceString() {
      return deviceString;
   }

   @Override public boolean equals(Object o) {
      if (this == o)
         return true;
      if (o == null || getClass() != o.getClass())
         return false;
      if (!super.equals(o))
         return false;
      InitiateConnectionsMessage that = (InitiateConnectionsMessage) o;
      return neighbors.equals(that.neighbors) && deviceString.equals(that.deviceString);
   }

   @Override public int hashCode() {
      return Objects.hash(super.hashCode(), neighbors, deviceString);
   }

   @Override public String toString() {
      return "InitiateConnectionsMessage{" + "neighbors=" + neighbors + ", deviceString='" + deviceString + '\'' + "} " + super.toString();
   }
}
