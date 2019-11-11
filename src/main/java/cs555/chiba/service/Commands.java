package cs555.chiba.service;

import cs555.chiba.util.Utilities;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * @author mmuller
 *
 * Base commands they all have.
 */
public class Commands {

   private static final Logger logger = Logger.getLogger(Commands.class.getName());

   private final Map<String, Function<String[], Void>> commands;

   private Commands(Map<String, Function<String[], Void>> coms) {
      this.commands = coms;
      addBaseCommands();
   }

   private void addBaseCommands() {
      this.commands.put("exit", args -> { // shutdown
         logger.info("Exit Command Received");
         return null;
      });
      this.commands.put("help", args -> { // list all commands
         logger.info(toString());
         return null;
      });
      this.commands.put("name", args -> { // who am I
         logger.info(ServiceNode.getThisNode().getIdentity().getIdentityKey());
         return null;
      });
      this.commands.put("listconnections", args -> { // list all the connections
         logger.info(listConnections());
         return null;
      });
   }

   private static String listConnections() {
      StringBuffer out = new StringBuffer("Connections in the Pool: \n");
      ServiceNode.getThisNode().getTcpConnectionsCache().listConnections().forEach(ident -> {
         out.append(ident.getIdentityKey()).append("\n");
      });

      return out.toString();
   }

   public void handleCommand(String command, String[] args) throws Exception {
      Function<String[], Void> func = this.commands.get(command.toLowerCase());

      if (func == null) {
         logger.severe("Received an Unimplemented Command: " + command + " with args: " + String.join(",", args));
      }
      else {
         func.apply(args);
      }
   }

   public Map<String, Function<String[], Void>> getCommands() {
      return commands;
   }

   @Override public String toString() {
      StringBuffer out = new StringBuffer("Available Commands: \n");

      this.commands.keySet().forEach(com -> {
         out.append(com).append("\n");
      });

      return out.toString();
   }

   @Override public boolean equals(Object o) {
      if (this == o)
         return true;
      if (o == null || getClass() != o.getClass())
         return false;
      Commands commands1 = (Commands) o;
      return Objects.equals(commands, commands1.commands);
   }

   @Override public int hashCode() {
      return Objects.hash(commands);
   }

   public static Builder builder() {
      return new Builder();
   }

   public static Builder builder(Commands commands) {
      return new Builder(commands);
   }

   public static final class Builder {

      private Map<String, Function<String[], Void>> commands = new HashMap<>();

      private Builder() {
      }

      private Builder(Commands coms) {
         if (coms != null) {
            withCommands(coms.getCommands());
         }
      }

      public Builder withCommands(Map<String, Function<String[], Void>> coms) {
         if (coms != null) {
            this.commands = coms;
         }
         return this;
      }

      public Builder registerCommand(String command, Function<String[], Void> func) {
         if (!Utilities.isBlank(command) && func != null) {
            this.commands.put(command.toLowerCase(), func);
         }

         return this;
      }

      public Commands build() {
         return new Commands(this.commands);
      }
   }
}

