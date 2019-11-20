/**
* The TCPServerThread holds the server socket and
* creates new RecieverThreads for incoming connections.
*
* @author  Kevin Bruhwiler
* @version 1.0
* @since   2019-06-11 
*/

package cs555.chiba.transport;

import cs555.chiba.service.Identity;
import cs555.chiba.wireformats.EventFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TCPServerThread implements Runnable, AutoCloseable {
    private static final Logger logger = Logger.getLogger(TCPServerThread.class.getName());

    private ServerSocket server;
    private TCPConnectionsCache connections;
    private EventFactory factory;
    private int port;
    private InetAddress addr;
    private boolean dead = false;


	@SuppressWarnings("static-access")
	public TCPServerThread(int port, TCPConnectionsCache connections, EventFactory factory){
        try {
            this.server = new ServerSocket(port);
            this.addr = server.getInetAddress().getLocalHost();
            this.port = server.getLocalPort();
        } catch (IOException e){
            logger.log(Level.SEVERE, "TCPServerThread() ", e);
        }
        this.connections = connections;
        this.factory = factory;
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted() && !this.dead) {
            try {
                //Wait for incoming connection. Add receiver to the cache when one comes in
                Socket socket = server.accept();
                Identity ident = Identity.builder().withSocketAddress(socket.getRemoteSocketAddress()).build();
                // the sender thread side of this is created after the introduction message is received
                connections.addReceiverThread(ident, new TCPReceiverThread(socket, factory));
            } catch (Exception e){
                if (!this.dead) {
                    logger.log(Level.SEVERE, "TCPServerThread.run() ", e);
                }
            }
        }
        close();
        logger.info("TCPServerThread Closed");
    }

    /**
     * Close the ServerSocket
     */
    @Override
    public void close() {
        try {
            this.dead = true;
            server.close();
            connections.close();
        } catch (IOException e){
            logger.log(Level.SEVERE, "Failed to Close", e);
        }
    }

    public int getPort() {
        return port;
    }

    public InetAddress getAddr() {
        return addr;
    }
}