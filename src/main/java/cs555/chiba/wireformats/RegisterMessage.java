package cs555.chiba.wireformats;

import cs555.chiba.service.Identity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class RegisterMessage extends Message {

   private Identity identity;
   private String deviceList;

   public RegisterMessage(Identity identity, String deviceList) {
      super(Protocol.REGISTER);
      this.identity = identity;
      this.deviceList = deviceList;
   }

   public RegisterMessage(byte[] message) throws IOException {
      super(Protocol.REGISTER, message);
   }

   @Override void parse(DataInputStream input) throws IOException {
      String key = readString(input);
      this.identity = Identity.builder().withIdentityKey(key).build();
      this.deviceList = readString(input);
   }

   @Override void spool(DataOutputStream output) throws IOException {
      sendString(this.identity.getIdentityName(), output);
      sendString(this.deviceList, output);
   }

   public Identity getIdentity() {
      return identity;
   }

   public String getDeviceList() {
      return deviceList;
   }

   @Override public boolean equals(Object o) {
      if (this == o)
         return true;
      if (o == null || getClass() != o.getClass())
         return false;
      if (!super.equals(o))
         return false;
      RegisterMessage that = (RegisterMessage) o;
      return identity.equals(that.identity) && deviceList.equals(that.deviceList);
   }

   @Override public int hashCode() {
      return Objects.hash(super.hashCode(), identity, deviceList);
   }

   @Override public String toString() {
      return "RegisterMessage{" + "identity=" + identity + ", deviceList='" + deviceList + '\'' + "} " + super.toString();
   }
}
