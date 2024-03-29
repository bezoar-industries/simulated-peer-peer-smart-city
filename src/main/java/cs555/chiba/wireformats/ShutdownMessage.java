package cs555.chiba.wireformats;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ShutdownMessage extends Message {

   public ShutdownMessage() {
      super(Protocol.SHUTDOWN);
   }

   public ShutdownMessage(byte[] message) throws IOException {
      super(Protocol.SHUTDOWN, message);
   }

   @Override void parse(DataInputStream input) throws IOException {

   }

   @Override void spool(DataOutputStream output) throws IOException {

   }

}
