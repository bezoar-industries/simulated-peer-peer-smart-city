package cs555.chiba.wireformats;

import cs555.chiba.service.Identity;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestListPeersResponseTransformer extends MessageTestCase {

   @Test public void testMarshalling() throws IOException {
      List<Identity> peers = buildPeerList();
      ListPeersResponseMessage message = new ListPeersResponseMessage(buildTestIdentity(5472), peers);
      testMarshallingOfMessage(message);
   }

   private List<Identity> buildPeerList() {
      List<Identity> idents = new ArrayList<>();
      idents.add(buildTestIdentity(5532));
      idents.add(buildTestIdentity(9785));
      idents.add(buildTestIdentity(3456));
      idents.add(buildTestIdentity(9675));
      return idents;
   }

   private Identity buildTestIdentity(int port) {
      return Identity.builder().withHost("www.test.com").withPort(port).build();
   }
}
