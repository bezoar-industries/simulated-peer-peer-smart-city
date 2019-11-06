/**
* The Peer class is the entry point for individual peers
* and manages setup and event handling.
*
* @author  Kevin Bruhwiler
* @version 1.0
* @since   2019-06-11 
*/

package cs555.chiba.node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import cs555.chiba.transport.TCPConnectionsCache;
import cs555.chiba.transport.TCPRecieverThread;
import cs555.chiba.transport.TCPSender;
import cs555.chiba.transport.TCPServerThread;
import cs555.chiba.util.InteractiveCommandParser;
import cs555.chiba.wireformats.Event;
import cs555.chiba.wireformats.EventFactory;
import cs555.chiba.wireformats.SampleMessage;

public class Peer implements Node {
	private Thread icp;
	private TCPServerThread server;
	private EventFactory eventFactory;
    private TCPConnectionsCache connections;
    private int myPort;
    private InetAddress myAddr;
    private Thread serverThread;
    private int id;

	public Peer(InetAddress registryHost, int registryPort, int id){
		//Set-up activities - get the event factory, create the ICP
		this.id = id;
        this.eventFactory = EventFactory.getInstance(this);
        this.icp = new Thread(new InteractiveCommandParser(this));
        this.icp.start();

        try {
            //Connect to registry and start thread
        	Socket registrySocket = new Socket(registryHost, registryPort);
            //add registry to connections cache - we will want to always keep this connection open
            this.connections = new TCPConnectionsCache(new TCPSender(registrySocket)); 
            Thread registryThread = new Thread(new TCPRecieverThread(registrySocket, eventFactory));
            this.connections.addRecieverThread(registryThread);
            registryThread.start();
        } catch (IOException e){
            System.out.println("Peer() " + e);
        }

        //Start a server socket so we can receive incoming connections
        this.server = new TCPServerThread(0, connections, eventFactory);
        this.myPort = server.getPort();
        this.myAddr = server.getAddr();
        this.serverThread = new Thread(server);
        serverThread.start();
        
        byte[] m = new SampleMessage(id).getBytes();
        //Send registration
        connections.send(m); 
    }
    
	/**
     * Convenience to sends a single message to an IP and Port without creating a new thread
     * WARNING - recipient will not be able to reply over the same socket
     * This should not be used for long-term connections
     * @param ip The InetAddress of the recipient
     * @param port The port of the recipient
     * @param m The serialized message to be sent
     */
    public void sendMessage(InetAddress ip, int port, byte[] m) {
    	try {
			TCPSender temp = new TCPSender(new Socket(ip, port));
			temp.sendMessage(m);
			temp.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    /**
     * A handler for SampleMessages
     * @param e The SampleMessage
     */
    public void SampleMessage(SampleMessage e){
        System.out.println("Recieved sample message with num: "+e.getNum());
    }
    
    /**
     * Routes messages to the correct handler
     * @param e The message that must be handled
     */
    @Override
    public void onEvent(Event e){
        if (e instanceof SampleMessage){ SampleMessage((SampleMessage)e); }
    }
    
    /**
     * A method to be called by the InteractiveCommandParser
     */
    public void sampleCommand() {
    	System.out.println("This is a sample command");
    }
	
    /**
     * Creates a new Peer, then idles until exit
     * @param args The IP address of the registry, the port of the registry, and this Peer's ID
     */
	public static void main(String[] args) {
		try {
			new Peer(InetAddress.getByName(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
		} catch (UnknownHostException e){
			System.out.println("Peer.main() " + e);
		}
        try {
        	//Idle - maybe add an "exit" command in the ICP?
            Thread.currentThread().join(); 
        } catch (InterruptedException e){ }

	}
}
