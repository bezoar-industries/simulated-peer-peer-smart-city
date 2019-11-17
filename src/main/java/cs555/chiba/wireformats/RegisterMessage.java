package cs555.chiba.wireformats;

import cs555.chiba.service.Identity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public class RegisterMessage extends Message {

   private Identity identity;

   public RegisterMessage(Identity identity) {
      super(Protocol.REGISTER);
      this.identity = identity;
   }

   public RegisterMessage(byte[] message) throws IOException {
      super(Protocol.REGISTER, message);
   }

   @Override void parse(DataInputStream input) throws IOException {
      String key = readString(input);
      this.identity = Identity.builder().withIdentityKey(key).build();
   }

   @Override void spool(DataOutputStream output) throws IOException {
      sendString(this.identity.getIdentityName(), output);
   }

   public Identity getIdentity() {
      return identity;
   }

   @Override public String toString() {
      return "RegisterMessage{" + "identity=" + identity + "} " + super.toString();
   }
}
