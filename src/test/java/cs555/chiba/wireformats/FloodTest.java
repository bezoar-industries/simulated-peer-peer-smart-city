package cs555.chiba.wireformats;

import cs555.chiba.service.Identity;
import org.junit.Test;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class FloodTest {
    @Test
    public void testMarshalling() throws IOException {
        Flood message = new Flood(
                UUID.randomUUID(),
                Identity.builder().withIdentityKey("testSender.com:8989").build(),
                Identity.builder().withIdentityKey("testOriginator.com:8989").build(),
                "temp", 1, 10);

        byte[] messageAsBytes = message.getBytes();

        Flood retranslatedMessage = new Flood(messageAsBytes, new Socket());

        assertEquals(retranslatedMessage.getCurrentHop(), message.getCurrentHop());
        assertEquals(retranslatedMessage.getHopLimit(), message.getHopLimit());
        assertEquals(retranslatedMessage.getID(), message.getID());
        assertEquals(retranslatedMessage.getOriginatorId(), message.getOriginatorId());
        assertEquals(retranslatedMessage.getSenderID(), message.getSenderID());
        assertEquals(retranslatedMessage.getTarget(), message.getTarget());

    }

}
