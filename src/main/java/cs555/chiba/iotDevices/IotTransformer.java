package cs555.chiba.iotDevices;

import cs555.chiba.util.Utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class IotTransformer {

   private String deviceString;
   private List<IotDevice> connectedIotDevices;

   public IotTransformer(List<IotDevice> connectedIotDevices) {
      this.connectedIotDevices = connectedIotDevices;
   }

   public IotTransformer(String deviceString) {
      this.deviceString = deviceString;
   }

   private String generateDeviceString() {
      int[] counts = new int[IotType.values().length];
      this.connectedIotDevices.forEach(iotDevice -> {
         counts[iotDevice.getType().ordinal()]++;
      });

      return Arrays.stream(counts).mapToObj(Integer::toString).collect(Collectors.joining(""));
   }

   public String getDeviceString() {
      if (this.deviceString == null) {
         this.deviceString = generateDeviceString();
      }

      return this.deviceString;
   }

   public List<IotDevice> getConnectedIotDevices() {
      if (this.connectedIotDevices == null) {
         this.connectedIotDevices = generateDevices();
      }

      return this.connectedIotDevices;
   }

   private List<IotDevice> generateDevices() {
      List<IotDevice> devices = new ArrayList<>();

      if (!Utilities.isBlank(this.deviceString)) {
         List<Integer> pieces = Arrays.stream(this.deviceString.split("")).map(Integer::parseInt).collect(Collectors.toList());

         int size = Math.min(pieces.size(), IotType.values().length);
         for (int i = 0; i < size; i++) {
            IotType type = IotType.values()[i];
            int count = pieces.get(i);
            for (int j = 0; j < count; j++) {
               devices.add(type.getInstance());
            }
         }
      }
      return devices;
   }
}
