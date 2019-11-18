package cs555.chiba.wireformats;

import cs555.chiba.service.ServiceNode;
import org.junit.Rule;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.doAnswer;

public abstract class MessageTestCase {

   private Message result = null;  // this is cheating.  Don't do this.

   Answer grabMessage = new Answer() {
      public Object answer(InvocationOnMock invocation) throws Throwable {
         result = invocation.getArgument(0);
         return null;
      }
   };

   Message testMarshallingOfMessage(Message message) throws IOException {
      ServiceNode mockNode = Mockito.mock(ServiceNode.class);
      doAnswer(grabMessage).when(mockNode).onEvent(any());

      Message transportedMessage = marshal(message, mockNode);

      assertEquals(message, transportedMessage);

      return transportedMessage;
   }

   Message marshal(Message message, ServiceNode node) throws IOException {
      EventFactory factory = EventFactory.getInstance(node);

      byte[] bytes = message.getBytes();

      factory.processMessage(bytes, null);

      return this.result;
   }


}
