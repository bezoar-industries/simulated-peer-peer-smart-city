package cs555.chiba.wireformats;

import cs555.chiba.service.Identity;
import org.junit.Test;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class GossipQueryTest {
    @Test
    public void testMarshalling() throws IOException {
        GossipQuery message = new GossipQuery(
                UUID.randomUUID(),
                Identity.builder().withIdentityKey("testSender.com:8989").build(),
                Identity.builder().withIdentityKey("testOriginator.com:8989").build(),
                "temp", 1, 10, 0);

        byte[] messageAsBytes = message.getBytes();

        GossipQuery retranslatedMessage = new GossipQuery(messageAsBytes, new Socket());

        assertEquals(retranslatedMessage.getCurrentHop(), message.getCurrentHop());
        assertEquals(retranslatedMessage.getHopLimit(), message.getHopLimit());
        assertEquals(retranslatedMessage.getID(), message.getID());
        assertEquals(retranslatedMessage.getOriginatorId(), message.getOriginatorId());
        assertEquals(retranslatedMessage.getSenderID(), message.getSenderID());
        assertEquals(retranslatedMessage.getTarget(), message.getTarget());
        assertEquals(retranslatedMessage.getGossipType(), message.getGossipType());

    }

}
