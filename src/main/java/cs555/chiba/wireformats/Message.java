package cs555.chiba.wireformats;

import cs555.chiba.service.Identity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class Message implements Event {

   private final int type;

   Message(Protocol type) {
      this.type = type.ordinal();
   }

   Message(Protocol type, byte[] message) throws IOException {
      this.type = type.ordinal();
      ByteArrayInputStream baInputStream = new ByteArrayInputStream(message);
      DataInputStream input = new DataInputStream(new BufferedInputStream(baInputStream));
      input.readByte();
      parse(input);
   }

   @Override public byte[] getBytes() throws IOException {
      ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
      DataOutputStream output = new DataOutputStream(new BufferedOutputStream(baOutputStream));
      output.writeByte(this.type);
      spool(output);
      output.flush();
      return baOutputStream.toByteArray();
   }

   abstract void parse(DataInputStream input) throws IOException;

   abstract void spool(DataOutputStream output) throws IOException;

   @Override public int getType() {
      return this.type;
   }

   void sendString(String field, DataOutputStream output) throws IOException {
      byte[] fieldBytes = field.getBytes();
      int fieldLength = fieldBytes.length;
      output.writeInt(fieldLength);
      output.write(fieldBytes);
   }

   String readString(DataInputStream input) throws IOException {
      int fieldLength = input.readInt();
      byte[] fieldBytes = new byte[fieldLength];
      input.readFully(fieldBytes);
      return new String(fieldBytes);
   }

   void sendListOfIdentities(List<Identity> identities, DataOutputStream output) throws IOException {
      output.writeInt(identities.size());
      for (Identity identity : identities) {
         sendString(identity.getIdentityKey(), output);
      }
   }

   List<Identity> readListOfIdentities(DataInputStream input) throws IOException {
      List<Identity> identities = new ArrayList<>();
      int total = input.readInt();

      if (total > 0) {
         for (int i = 0; i < total; i++) {
            identities.add(Identity.builder().withIdentityKey(readString(input)).build());
         }
      }

      return identities;
   }
}
