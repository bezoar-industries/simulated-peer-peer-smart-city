package cs555.chiba.wireformats;

import org.junit.Test;

import java.io.IOException;

public class TestListPeersRequestTransformer extends MessageTestCase {

   @Test public void testMarshalling() throws IOException {
      ListPeersRequestMessage message = new ListPeersRequestMessage();
      testMarshallingOfMessage(message);
   }

}
