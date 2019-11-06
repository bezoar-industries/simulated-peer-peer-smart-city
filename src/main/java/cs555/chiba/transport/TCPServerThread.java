/**
* The TCPServerThread holds the server socket and
* creates new RecieverThreads for incoming connections.
*
* @author  Kevin Bruhwiler
* @version 1.0
* @since   2019-06-11 
*/

package cs555.chiba.transport;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import cs555.chiba.wireformats.EventFactory;


public class TCPServerThread implements Runnable{
    private ServerSocket server;
    private TCPConnectionsCache connections;
    private EventFactory factory;
    private int port;
    private InetAddress addr;


	@SuppressWarnings("static-access")
	public TCPServerThread(int port, TCPConnectionsCache connections, EventFactory factory){
        try {
            this.server = new ServerSocket(port);
            this.addr = server.getInetAddress().getLocalHost();
            this.port = server.getLocalPort();
        } catch (IOException e){
            System.out.println("TCPServerThread() " + e);
        }
        this.connections = connections;
        this.factory = factory;
    }

    public void run() {
        while (true) {
            try {
                //Wait for incoming connection. Add receiver to the cache when one comes in
                Socket socket = server.accept();
                Thread recieverThread = new Thread(new TCPRecieverThread(socket, factory));
                connections.addRecieverThread(recieverThread);
                recieverThread.start();
            } catch (IOException e){
                System.out.println("TCPServerThread.run() " + e);
            }
        }
    }

    /**
     * Close the ServerSocket
     */
    public void close() {
        try {
            server.close();
        } catch (IOException e){
            System.out.println(e);
        }
    }

    public int getPort() {
        return port;
    }

    public InetAddress getAddr() {
        return addr;
    }
}