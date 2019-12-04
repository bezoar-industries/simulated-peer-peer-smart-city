package cs555.chiba.service;

import cs555.chiba.transport.TCPConnectionsCache;
import cs555.chiba.transport.TCPServerThread;
import cs555.chiba.wireformats.Event;
import cs555.chiba.wireformats.EventFactory;

import java.io.Console;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author mmuller
 *
 * The main Service Node class.  This is the back end for all the other Nodes
 * It runs a user command prompt.
 */
public abstract class ServiceNode {

   private static final Logger logger = Logger.getLogger(ServiceNode.class.getName());
   private static ServiceNode myself;
   private static Thread commandPromptThread;

   private Identity identity; // who am I?
   private EventFactory eventFactory;
   private TCPConnectionsCache connections;
   private TCPServerThread server;
   private Thread serverThread;
   private boolean dead = false;

   public static ServiceNode getThisNode() {
      return myself;
   }

   public Identity getIdentity() {
      return identity;
   }

   public EventFactory getEventFactory() {
      return this.eventFactory;
   }

   public abstract void onEvent(Event e);

   /**
    * The 'infinite' loop.  Block waiting for a command.  Handle the command, block again.
    * The defined exit command will interrupt the thread.
    */
   private void runCommandPromptLoop(Commands commands, String prompt) throws InterruptedException {
      commandPromptThread = Thread.currentThread();
      while (!Thread.currentThread().isInterrupted() && !this.dead) {
         String userInput = readFromPrompt(prompt);

         if (userInput != null && userInput.trim().length() > 0) {
            parseCommand(commands, userInput.trim().split(" "));
         }
      }
   }

   /**
    * Java's magic for reading user input.  Unfortunately, there doesn't appear to be a way to support the arrow keys for history.
    */
   private String readFromPrompt(String prompt) throws InterruptedException {
      Console c = System.console();

      if (c == null) {
         logger.severe("Unable to open system console! Waiting on interrupt.");
         Thread.currentThread().join(); // sleep forever... or until ctl-c interrupts it
         return null;
      }

      return c.readLine(prompt);
   }

   /**
    * Convert the typed command to a valid Enum for execution
    */
   private void parseCommand(Commands commands, String[] args) {
      String command = args[0];

      if (command == null) {
         logger.severe("Unknown Command");
      }
      else {
         // handle the command, pass in the args without the leading command
         try {
            commands.handleCommand(command.toLowerCase().replaceAll("[- _]]", ""), Arrays.copyOfRange(args, 1, args.length));
         }
         catch (Exception e) {
            logger.log(Level.SEVERE, "Command [" + command + "]  with args [" + String.join(", ", args) + "] threw an Exception", e);
         }
      }

      // leave the loop once the exit command has been issued
      if (command.toLowerCase().startsWith("exit")) {
         Thread.currentThread().interrupt();
      }

   }

   private void startup(int port, Commands commands, String prompt) {
      try {
         myself = this;
         this.eventFactory = EventFactory.getInstance(this);
         this.connections = new TCPConnectionsCache();
         this.server = new TCPServerThread(port, connections, eventFactory);
         this.identity = Identity.builder().withHost(this.server.getAddr().getHostAddress()).withPort(this.server.getPort()).build();
         this.serverThread = new Thread(this.server);
         this.serverThread.start();
         specialStartUp();
         logger.info("Starting up as [" + this.getIdentity().getIdentityKey() + "]");
         runCommandPromptLoop(commands, prompt); // start printing metrics every 20 seconds
      }
      catch (InterruptedException e) {
         // We were told to shut down
      }
      catch (Exception e) {
         logger.log(Level.SEVERE, "Something Terrible Happened!", e);
      }
      finally {
         shutdown();
      }
   }

   private void automatedStartup(int port, Commands commands, int minConnections, int maxConnections, String filename) {
      try {
         myself = this;
         this.eventFactory = EventFactory.getInstance(this);
         this.connections = new TCPConnectionsCache();
         this.server = new TCPServerThread(port, connections, eventFactory);
         this.identity = Identity.builder().withHost(this.server.getAddr().getHostAddress()).withPort(this.server.getPort()).build();
         this.serverThread = new Thread(this.server);
         this.serverThread.start();
         specialStartUp();
         logger.info("Starting up as [" + this.getIdentity().getIdentityKey() + "]");
         int[] maxHops = new int[]{14,13,12,11,10,9,8,7,6,5,4};
         String[] queryTypes = new String[]{"flood","gossiptype0","gossiptype1","randomwalk"};
         synchronized(this) {
        	 this.wait(30000);
			 logger.info("building overlay");
			 parseCommand(commands, new String[]{"buildoverlay", ""+minConnections, ""+maxConnections});
		     parseCommand(commands, new String[]{"connectpeers"});
		     this.wait(30000);
		     parseCommand(commands, new String[]{"flood", "AIR_QUALITY", "-1"});
		     this.wait(20000);
		     for (int maxHop : maxHops) {
		        for(int i = 0; i < 6; i++) {
		           for (String type : queryTypes) {
		              parseCommand(commands, new String[]{type, "AIR_QUALITY", "" + maxHop});
		              this.wait(500);
		           }
		        }
	         }
	         this.wait(5000);
		 }
         parseCommand(commands, new String[]{"export-results", filename});
      }
      catch (InterruptedException e) {
         // We were told to shut down
      }
      catch (Exception e) {
         logger.log(Level.SEVERE, "Something Terrible Happened!", e);
      }
      finally {
         shutdown();
      }
   }

   /**
    * Execute the given command.
    */
   protected void specialStartUp() throws Exception {
      // do nothing
   }

   public void shutdown() {
      specialShutdown();
      this.server.close();
      this.serverThread.interrupt();
      this.connections.close();
      this.dead = true;
      commandPromptThread.interrupt();
   }

   protected void specialShutdown() {
      // do nothing
   }

   public TCPConnectionsCache getTcpConnectionsCache() {
      return this.connections;
   }

   protected static void startup(int port, ServiceNode node, Commands commands, String prompt) {
      addShutdownHook(node);
      node.startup(port, commands, prompt);
   }

   protected static void automatedStartup(int port, ServiceNode node, Commands commands, int minConnections, int maxConnections, String filename) {
      addShutdownHook(node);
      node.automatedStartup(port, commands, minConnections, maxConnections, filename);
   }

   // Close everything nicely when ctl C or the exit command is sent.
   private static void addShutdownHook(ServiceNode server) {
      Runtime.getRuntime().addShutdownHook(new Thread() {

         public void run() {
            try {
               logger.info("Shutting down...");
               server.shutdown();
            }
            catch (Exception e) {
               logger.log(Level.SEVERE, "Shutdown failed", e);
            }
         }
      });
   }



   public static <T extends ServiceNode> T getThisNodeOrNull(Class<T> nodeType) {
      if (nodeType.isInstance(myself)) {
         return nodeType.cast(myself);
      }

      return null;
   }

   public static <T extends ServiceNode> T getThisNode(Class<T> nodeType) {
      return nodeType.cast(myself);
   }

   public void removeConnection(Identity identity) {
      this.connections.removeConnection(identity);
   }
}
