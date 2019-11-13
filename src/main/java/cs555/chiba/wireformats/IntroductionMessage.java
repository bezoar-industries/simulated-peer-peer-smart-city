package cs555.chiba.wireformats;

import cs555.chiba.service.Identity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public class IntroductionMessage extends Message {

   private Identity identity;
   private UUID uuid;

   public IntroductionMessage(Identity identity, UUID uuid) {
      super(Protocol.REGISTER);
      this.identity = identity;
      this.uuid = uuid;
   }

   public IntroductionMessage(byte[] message) throws IOException {
      super(Protocol.REGISTER, message);
   }

   @Override void parse(DataInputStream input) throws IOException {
      String key = readString(input);
      String uuid = readString(input);
      this.identity = Identity.builder().withIdentityKey(key).build();
      this.uuid = UUID.fromString(uuid);
   }

   @Override void spool(DataOutputStream output) throws IOException {
      sendString(this.identity.getIdentityKey(), output);
      sendString(this.uuid.toString(), output);
   }

   public Identity getIdentity() {
      return identity;
   }

   public UUID getUuid() {
      return this.uuid;
   }

   @Override public String toString() {
      return "IntroductionMessage{" + "identity=" + identity + ", uuid=" + uuid + "} " + super.toString();
   }
}
