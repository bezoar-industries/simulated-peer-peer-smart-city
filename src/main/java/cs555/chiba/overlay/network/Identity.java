package cs555.chiba.overlay.network;

import cs555.chiba.util.Utilities;

import java.net.InetSocketAddress;

/**
 * @author mmuller
 *
 * Tracking who a connection is connected to requires a few different forms of the same data.  Rather than passing the pieces, this 
 * class maintains all the necessary identity information.
 */
public class Identity {

   private final String identityKey; // used to print the name and as a key in maps.
   private final String host; // host and port used in various messages and command line activites
   private final int port;
   private final InetSocketAddress socketAddress; // used to resolve the given id to confirm validity of the identity. TODO: Remove this.  Resolving host name takes way too long, and it isn't really necessary.

   Identity(String identityKey, String host, int port, InetSocketAddress socketAddress) {
      this.identityKey = identityKey;
      this.host = host;
      this.port = port;
      this.socketAddress = socketAddress;
   }

   public String getIdentityKey() {
      return identityKey;
   }

   public String getHost() {
      return host;
   }

   public int getPort() {
      return port;
   }

   public InetSocketAddress getSocketAddress() {
      return socketAddress;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((host == null) ? 0 : host.hashCode());
      result = prime * result + ((identityKey == null) ? 0 : identityKey.hashCode());
      result = prime * result + port;
      result = prime * result + ((socketAddress == null) ? 0 : socketAddress.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      Identity other = (Identity) obj;
      if (host == null) {
         if (other.host != null)
            return false;
      }
      else if (!host.equals(other.host))
         return false;
      if (identityKey == null) {
         if (other.identityKey != null)
            return false;
      }
      else if (!identityKey.equals(other.identityKey))
         return false;
      if (port != other.port)
         return false;
      if (socketAddress == null) {
         if (other.socketAddress != null)
            return false;
      }
      else if (!socketAddress.equals(other.socketAddress))
         return false;
      return true;
   }

   @Override
   public String toString() {
      return "Identity [identityKey=" + identityKey + ", host=" + host + ", port=" + port + ", socketAddress=" + socketAddress + "]";
   }

   public static Builder builder() {
      return new Builder();
   }

   public static Builder builder(Identity ident) {
      return new Builder(ident);
   }

   public static final class Builder {

      private String identityKey;
      private String host;
      private int port;
      private InetSocketAddress socketAddress;

      private Builder() {
      }

      private Builder(Identity ident) {
         if (ident != null) {
            withIdentityKey(ident.getIdentityKey());
            withHost(ident.getHost());
            withPort(ident.getPort());
            withSocketAddress(ident.getSocketAddress());
         }
      }

      public Builder withIdentityKey(String identityKey) {
         this.identityKey = identityKey;
         return this;
      }

      public Builder withHost(String host) {
         this.host = host;
         return this;
      }

      public Builder withPort(int port) {
         this.port = port;
         return this;
      }

      public Builder withPort(String port) {
         this.port = Utilities.parsePort(port);
         return this;
      }

      public Builder withSocketAddress(InetSocketAddress socketAddress) {
         this.socketAddress = socketAddress;
         return this;
      }

      public Identity build() {
         fixMissingFields();
         Utilities.checkArgument(!Utilities.isBlank(this.identityKey), "Identity Key cannot be blank");
         Utilities.checkArgument(!Utilities.isBlank(this.host), "Identity Key cannot be blank");
         Utilities.checkArgument(Utilities.parsePort(port) != 0, "Invalid port");

         return new Identity(this.identityKey, this.host, this.port, this.socketAddress);
      }

      /**
       * Identity can be built from host/port, identity key, or the socket address.  Build all 3 before creating the Identity Object.
       */
      private void fixMissingFields() {
         fixIdentityKey();
         fixHostAndPort();
         fixSocketAddress();
      }

      private void fixIdentityKey() {
         if (!Utilities.isBlank(this.identityKey)) {
            return;
         }
         else if (!Utilities.isBlank(this.host) && this.port != 0) {
            this.identityKey = this.host + ":" + this.port;
         }
         else if (this.socketAddress != null) {
            this.identityKey = this.socketAddress.getHostString() + ";" + this.socketAddress.getPort();
         }

         Utilities.checkArgument(!Utilities.isBlank(this.identityKey), "Identity Key cannot be blank");
      }

      private void fixHostAndPort() {
         String[] pieces = this.identityKey.split(":");
         if (Utilities.isBlank(this.host)) {
            this.host = pieces[0];
            this.port = Utilities.parsePort(pieces[1]);
         }
         else {
            Utilities.checkArgument(this.identityKey.equals(this.host + ":" + this.port), "The host [" + this.host + "] and port [" + this.port + "] dont't match the identity key [" + this.identityKey + "]");
         }
      }

      private void fixSocketAddress() {
         if (this.socketAddress != null) {
            Utilities.checkArgument(this.host.equals(this.socketAddress.getHostName()), "The socket address [" + this.socketAddress + "] doesn't match the host [" + this.host + "]");
            Utilities.checkArgument(this.port == this.socketAddress.getPort(), "The socket address [" + this.socketAddress + "] doesn't match the port [" + this.port + "]");
         }
         else {
            this.socketAddress = new InetSocketAddress(this.host, this.port);
         }
      }
   }
}
