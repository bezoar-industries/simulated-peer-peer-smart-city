package cs555.chiba.iotDevices;

import org.junit.Test;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class TestIotTransformer {

   @Test public void generateDeviceString() {
      List<IotDevice> connectedIotDevices = IotFactory.generateRandomDevices(300, 3000);
      IotTransformer trans = new IotTransformer(connectedIotDevices);
      String devices = trans.getDeviceString();
      trans = new IotTransformer(devices);

      List<String> expected = connectedIotDevices.stream().map(IotDevice::toString).collect(Collectors.toList());
      List<String> actual = trans.getConnectedIotDevices().stream().map(IotDevice::toString).collect(Collectors.toList());
      Collections.sort(expected);
      Collections.sort(actual);

      assertEquals(expected, actual);
   }
}