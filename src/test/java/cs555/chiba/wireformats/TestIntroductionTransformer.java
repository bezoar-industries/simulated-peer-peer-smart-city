package cs555.chiba.wireformats;

import cs555.chiba.service.Identity;
import org.junit.Test;

import java.io.IOException;

public class TestIntroductionTransformer extends MessageTestCase {

   @Test public void testMarshalling() throws IOException {
      IntroductionMessage message = new IntroductionMessage(Identity.builder().withIdentityKey("test.com:8989").build());
      testMarshallingOfMessage(message);
   }

}
