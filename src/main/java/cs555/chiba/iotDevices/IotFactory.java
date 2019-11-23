package cs555.chiba.iotDevices;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public enum IotFactory {
   ;

   public static List<IotDevice> generateRandomDevices(int min, int max) {
      List<IotDevice> devices = new LinkedList<>();
      int deviceCount = ThreadLocalRandom.current().nextInt(min, max + 1);

      for (int i = 0; i < deviceCount; i++) {
         devices.add(generateRandomDevice());
      }

      return devices;
   }

   private static IotDevice generateRandomDevice() {
      IotType[] types = IotType.values();
      IotType type = types[ThreadLocalRandom.current().nextInt(types.length)];

      return type.getInstance();
   }
}
