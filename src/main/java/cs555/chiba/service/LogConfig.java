package cs555.chiba.service;

import cs555.chiba.util.Utilities;

import java.time.LocalDateTime;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * @author mmuller
 *
 * System.out deosn't have enough control to handle hundreds of processes.  This is the configuration for java's built in logger.
 * 
 * INFO level and up is printed to the console.
 * FINEST level and up is sent to the defined log file
 * 
 * If no file is defined, then INFO is the only thing printed.
 */
public class LogConfig {

   public LogConfig() {
      try {
         //System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %4$-7s [%3$s] (%2$s) %5$s %6$s%n");
         System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s %6$s%n"); // 5 is the message, 6 is the throwable if any
         SimpleFormatter consoleFormatter = new SimpleFormatter(); // Format for STDOUT
         LogFormatter fileFormatter = new LogFormatter(); // Format for logs in the file

         ConsoleHandler consoleHandler = new ConsoleHandler();
         consoleHandler.setLevel(Level.INFO); // log only INFO+ level things to Console
         consoleHandler.setFormatter(consoleFormatter);

         final Logger app = Logger.getLogger("cs555");
         app.setLevel(Level.FINEST); // Allow all logs to be processed in the cs555 packages
         app.addHandler(consoleHandler);

         // Get the log file.  If it exists, log to it.
         String logFilename = getFileName();

         if (!Utilities.isBlank(logFilename)) {
            FileHandler fileHandler = new FileHandler(logFilename, false); // The file is set not to append, so it will be replaced every time it is run
            fileHandler.setLevel(Level.FINE); // to log everything to the file, set this to finest
            fileHandler.setFormatter(fileFormatter);
            app.addHandler(fileHandler);
         }
      }
      catch (Exception e) {
         // The runtime won't show stack traces if the exception is thrown
         e.printStackTrace();
      }
   }

   private String getFileName() {
      String logFilename = System.getProperty("csu.log.file"); // was the log file defined

      // if the log file is for a client, add a timestamp so it doesn't overwrite other clients
      if (!Utilities.isBlank(logFilename) && logFilename.toLowerCase().contains("peer")) {
         int index = logFilename.indexOf(".");
         String prefix = logFilename.substring(0, index) + "-";
         String suffix = logFilename.substring(index);
         return prefix + LocalDateTime.now() + suffix;
      }

      return logFilename;
   }
}
