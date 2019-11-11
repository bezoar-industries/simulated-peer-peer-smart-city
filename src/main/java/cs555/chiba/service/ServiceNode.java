package cs555.chiba.service;

import cs555.chiba.node.Node;
import cs555.chiba.transport.TCPConnectionsCache;
import cs555.chiba.transport.TCPServerThread;
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
public abstract class ServiceNode implements Node {

   private static final Logger logger = Logger.getLogger(ServiceNode.class.getName());

   protected static void startup(int port, ServiceNode node, Commands commands, String prompt) {
      addShutdownHook(node);
      node.startup(port, commands, prompt);
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

   private static ServiceNode myself;
   private Identity identity; // who am I?
   private EventFactory eventFactory;
   private TCPConnectionsCache connections;
   private TCPServerThread server;
   private Thread serverThread;
   private boolean dead = false;

   public static <T extends ServiceNode> T getThisNodeOrNull(Class<T> nodeType) {
      if (nodeType.isInstance(myself)) {
         return nodeType.cast(myself);
      }

      return null;
   }

   public static <T extends ServiceNode> T getThisNode(Class<T> nodeType) {
      return nodeType.cast(myself);
   }

   public static ServiceNode getThisNode() {
      return myself;
   }

   public Identity getIdentity() {
      return identity;
   }

   public void setIdentity(Identity ident) {
      if (ident == null) {
         throw new IllegalArgumentException("The the node's identity cannot be null.");
      }

      identity = ident;
   }

   // FOR TESTING ONLY
   public static void setMyself(ServiceNode node) {
      myself = node;
   }

   /**
    * The 'infinite' loop.  Block waiting for a command.  Handle the command, block again.
    * The defined exit command will interrupt the thread.
    */
   private void runCommandPromptLoop(Commands commands, String prompt) throws InterruptedException {
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
   private static String readFromPrompt(String prompt) throws InterruptedException {
      Console c = System.console();

      if (c == null) {
         logger.severe("Unable to open system console! Waiting on interrupt.");
         Thread.sleep(Long.MAX_VALUE); // sleep forever... or until ctl-c interrupts it
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
         this.identity = Identity.builder().withHost(this.server.getAddr().getHostName()).withPort(this.server.getPort()).build();
         this.serverThread = new Thread(this.server);
         this.serverThread.start();
         specialStartUp();
         runCommandPromptLoop(commands, prompt); // start printing metrics every 20 seconds
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
      this.dead = true;
   }

   protected void specialShutdown() {
      // do nothing
   }

   public TCPConnectionsCache getTcpConnectionsCache() {
      return this.connections;
   }
}