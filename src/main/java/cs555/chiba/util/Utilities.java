package cs555.chiba.util;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Utilities {

   public enum METRIC_TYPES {POWER_CONSUPMTION, POWER_STATE, AIR_QUALITY, TEMPERATURE, THROTTLE_STATE, BATTERY_PERCENTAGE, CURRENT_TIME, TIME_SINCE_LAST_SYNC, LOCK_STATE,
                                INSIDE_TEMPERATURE, OPEN_STATE, CURRENT_CYCLE_STEP, LIGHT_STATUS, HOUSE_POWER_STATUS, FRIDGE_TEMPERATURE, FREEZER_TEMPERATURE, ICE_LEVEL,
                                RECORD_HIGH, RECORD_LOW, SET_TEMPERATURE, TIME_TO_NEXT_TERMPERATURE_CHANGE, CPU_USAGE, MEMORY_USAGE, NETFLIX, AMAZON_PRIME, HEART_RATE,
                                CURRENT_LEAK, };

   private static final Logger logger = Logger.getLogger(Utilities.class.getName());

   /**
    * Ensure the the correct number of arguments have been submitted, and that they are non-blank.
    */
   public static boolean checkArgCount(int count, String[] args) {
      if (args.length < count) {
         return false;
      }

      if (Arrays.stream(args).anyMatch(Utilities::isBlank)) {
         return false;
      }

      return true;
   }

   /**
    * Check a string to be either null or have no non whitespace characters.
    */
   public static boolean isBlank(String check) {
      if (check == null) {
         return true;
      }

      if (check.trim().length() < 1) {
         return true;
      }

      return false;
   }

   /**
    * Utility function for checking arguments for correctness.  Increases readability.  See the Message classes for examples.
    */
   public static void checkArgument(boolean check, String errorMessage) {
      if (!check) {
         throw new IllegalArgumentException(errorMessage);
      }

   }

   /**
    * Is this a valid port? Technically registered ports are from 1024–49151, but the full range, 1024 - 65535, is available.
    */
   public static int parsePort(int port) {
      if (port < 1024 || port > 65535) {
         throw new IllegalArgumentException("The port value is outside registered port ranges 1024 - 65535: " + port);
      }
      return port;
   }

   public static int parsePort(String userPort) {
      try {
         int port = Integer.parseInt(userPort);

         return parsePort(port);
      }
      catch (NumberFormatException e) {
         throw new IllegalArgumentException("The supplied port is invalid: " + userPort, e);
      }
   }

   /**
    * Read a positive integer from a string. Convenience method for dealing with NumberFormatExceptions.
    */
   public static int parsePositiveIntFromArg(String name, String snum) {
      try {
         int num = Integer.parseInt(snum);

         if (num < 1) {
            throw new IllegalArgumentException("The " + name + " must be above 0: " + num);
         }

         return num;
      }
      catch (NumberFormatException e) {
         throw new IllegalArgumentException("The " + name + " is invalid: " + snum, e);
      }
   }

   /**
    * Read a positive integer from a string. Convenience method for dealing with NumberFormatExceptions.
    */
   public static float parsePositiveFloatFromArg(String name, String snum) {
      try {
         float num = Float.parseFloat(snum);

         if (num < 0.0001) {
            throw new IllegalArgumentException("The " + name + " must be above 0.0001: " + num);
         }

         return num;
      }
      catch (NumberFormatException e) {
         throw new IllegalArgumentException("The " + name + " is invalid: " + snum, e);
      }
   }

   /**
    * Close the thing without throwing an exception.  Log it instead.
    */
   public static void closeQuietly(Closeable thing) {
      try {
         if (thing != null) {
            thing.close();
         }
      }
      catch (IOException e) {
         logger.log(Level.SEVERE, "The closeable thing didn't close correctly!", e);
      }
   }

   /**
    * Read an integer from a string.  Convenience method for dealing with checked NumberFormatExceptions.
    */
   public static int quietlyParseInt(String snum, int defaultNum) {
      try {
         return Integer.parseInt(snum);
      }
      catch (NumberFormatException e) {
         logger.log(Level.SEVERE, "Unable to parse int", e);
      }

      return defaultNum;
   }

   /**
    * Convenience method for converting a string to an Enum.
    */
   public static final <E extends Enum<E>> E getEnum(Class<E> clazz, String text) {
      for (E state : clazz.getEnumConstants()) {
         if (state.toString().equalsIgnoreCase(text)) {
            return state;
         }
      }

      return null;
   }

   /**
    * Calculate the SHA-1 (160 bit) checksum for supplied input.
    */
   public static byte[] calculateChecksums(byte[] input) {
      try {
         MessageDigest md = MessageDigest.getInstance("SHA-1");
         return md.digest(input);
      }
      catch (NoSuchAlgorithmException e) {
         logger.log(Level.SEVERE, "Basic crypto algorithm missing SHA-1", e);
      }

      return null; // this won't happen (famous last words)
   }

   /**
    * Grab the current time in LocalDateTime format
    */
   public static LocalDateTime now() {
      return time(System.currentTimeMillis());
   }

   /**
    * Convert long to LocalDateTime
    */
   public static LocalDateTime time(long millis) {
      return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), TimeZone.getDefault().toZoneId());
   }

   /**
    * list.copyOf isn't available in Java 8
    */
   public static <T> List<T> copy(Collection<T> list) {
      return Collections.unmodifiableList(new ArrayList<>(list));
   }

   public static <T> List<T> copy(Set<T> set) {
      return Collections.unmodifiableList(new ArrayList<>(set));
   }

   public static <K, V> Map<K, V> copy(Map<K, V> set) {
      return Collections.unmodifiableMap(new HashMap<>(set));
   }

   /**
    * This method converts a set of bytes into a Hexadecimal representation.
    */
   public static String convertBytesToHex(byte[] buf) {
      StringBuffer strBuf = new StringBuffer();
      for (int i = 0; i < buf.length; i++) {
         int byteValue = (int) buf[i] & 0xff;
         if (byteValue <= 15) {
            strBuf.append("0");
         }
         strBuf.append(Integer.toString(byteValue, 16));
      }
      return strBuf.toString();
   }

   /**
    * This method converts a specified hexadecimal String into a set of bytes.
    */
   public static byte[] convertHexToBytes(String hexString) {
      int size = hexString.length();

      if (size % 2 != 0) {
         hexString = "0" + hexString;
         size = hexString.length();
      }

      byte[] buf = new byte[size / 2];
      int j = 0;
      for (int i = 0; i < size; i++) {
         String a = hexString.substring(i, i + 2);
         int valA = Integer.parseInt(a, 16);
         i++;
         buf[j] = (byte) valA;
         j++;
      }
      return buf;
   }

   /**
    * This method converts a specified hexadecimal String into an int
    */
   public static int convertHexToInt(String hexString) {
      BigInteger value = new BigInteger(1, convertHexToBytes(hexString));
      return value.intValue();
   }

   /**
    * Read a file from the HD
    */
   public static byte[] readFile(File file) throws IOException {
      Path p = file.toPath();
      return Files.readAllBytes(p);
   }

   /**
    * Write this file to the HD
    */
   public static File writeFile(String filePath, String data) throws IOException {
      File file = new File(filePath);

      if (!file.exists()) {
         file.createNewFile();
      }

      if (!file.exists() || file.isDirectory() || !file.setWritable(true) || !file.canWrite()) {
         throw new IOException("Cannot write to file [" + file.getAbsolutePath() + "]");
      }

      try (RandomAccessFile raf = new RandomAccessFile(file.getAbsolutePath(), "rw")) {
         raf.setLength(0); // clear file before writing
         raf.write(data.getBytes());
      }

      return file;
   }
}
