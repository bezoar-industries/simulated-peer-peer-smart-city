package cs555.chiba.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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

   protected void sendString(String field, DataOutputStream output) throws IOException {
      byte[] fieldBytes = field.getBytes();
      int fieldLength = fieldBytes.length;
      output.writeInt(fieldLength);
      output.write(fieldBytes);
   }

   protected String readString(DataInputStream input) throws IOException {
      int fieldLength = input.readInt();
      byte[] fieldBytes = new byte[fieldLength];
      input.readFully(fieldBytes);
      return new String(fieldBytes);
   }
}
