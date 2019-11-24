package cs555.chiba.wireformats;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ListPeersRequestMessage extends Message {

   public ListPeersRequestMessage() {
      super(Protocol.LIST_PEERS_REQUEST);
   }

   public ListPeersRequestMessage(byte[] message) throws IOException {
      super(Protocol.LIST_PEERS_REQUEST, message);
   }

   @Override void parse(DataInputStream input) throws IOException {

   }

   @Override void spool(DataOutputStream output) throws IOException {

   }

}
