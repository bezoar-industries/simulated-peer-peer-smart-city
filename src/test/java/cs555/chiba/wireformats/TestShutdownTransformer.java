package cs555.chiba.wireformats;

import cs555.chiba.service.Identity;
import org.junit.Test;

import java.io.IOException;

public class TestShutdownTransformer extends MessageTestCase {

   @Test public void testMarshalling() throws IOException {
      ShutdownMessage message = new ShutdownMessage();
      testMarshallingOfMessage(message);
   }

}
