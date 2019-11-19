package cs555.chiba.wireformats;

import cs555.chiba.service.Identity;
import org.junit.Test;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

import static org.junit.Assert.*;

public class RandomWalkTest {
    @Test
    public void testMarshalling() throws IOException {
        RandomWalk message = new RandomWalk(
                UUID.randomUUID(),
                Identity.builder().withIdentityKey("testSender.com:8989").build(),
                Identity.builder().withIdentityKey("testOriginator.com:8989").build(),
                "temp", 1, 10);

        byte[] messageAsBytes = message.getBytes();

        RandomWalk retranslatedMessage = new RandomWalk(messageAsBytes, new Socket());

        assertEquals(retranslatedMessage.getCurrentHop(), message.getCurrentHop());
        assertEquals(retranslatedMessage.getHopLimit(), message.getHopLimit());
        assertEquals(retranslatedMessage.getID(), message.getID());
        assertEquals(retranslatedMessage.getOriginatorId(), message.getOriginatorId());
        assertEquals(retranslatedMessage.getSenderID(), message.getSenderID());
        assertEquals(retranslatedMessage.getTarget(), message.getTarget());

    }

}
