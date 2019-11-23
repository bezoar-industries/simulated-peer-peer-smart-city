package cs555.chiba.wireformats;

import cs555.chiba.iotDevices.IotDevice;
import cs555.chiba.iotDevices.IotFactory;
import cs555.chiba.iotDevices.IotTransformer;
import cs555.chiba.service.Identity;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class TestRegisterTransformer extends MessageTestCase {

   @Test public void testMarshalling() throws IOException {
      List<IotDevice> connectedIotDevices = IotFactory.generateRandomDevices(3, 30);
      IotTransformer trans = new IotTransformer(connectedIotDevices);
      RegisterMessage message = new RegisterMessage(Identity.builder().withIdentityKey("test.com:8989").build(), trans.getDeviceString());
      testMarshallingOfMessage(message);
   }
}
