package cs555.chiba.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * @author mmuller
 *
 * Custom format for logs sent to the log file.  The thread name has been added to debug race conditions.
 */
public class LogFormatter extends Formatter {

   private static final MessageFormat messageFormat = new MessageFormat("[{3,date,hh:mm:ss} [{1}] {2} {0} {5}] {4} {6}\n");

   public LogFormatter() {
      super();
   }

   @Override
   public String format(LogRecord record) {
      Object[] arguments = new Object[7];
      arguments[0] = record.getLoggerName();
      arguments[1] = record.getLevel();
      arguments[2] = Thread.currentThread().getName();
      arguments[3] = new Date(record.getMillis());
      arguments[4] = record.getMessage();
      arguments[5] = record.getSourceMethodName();
      arguments[6] = record.getThrown() == null ? "" : printStackTrace(record.getThrown());
      return messageFormat.format(arguments);
   }

   /**
    * Print the stack trace to a string, so it can be formatted in the log file.
    */
   private String printStackTrace(Throwable e) {
      StringWriter out = new StringWriter();
      PrintWriter writer = new PrintWriter(out);

      e.printStackTrace(writer);
      return "\n" + out.toString();
   }

}
