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
import java.util.UUID;

import cs555.chiba.transport.TCPConnectionsCache;
import cs555.chiba.transport.TCPRecieverThread;
import cs555.chiba.transport.TCPSender;
import cs555.chiba.transport.TCPServerThread;
import cs555.chiba.util.InteractiveCommandParser;
import cs555.chiba.util.LRUCache;
import cs555.chiba.wireformats.Event;
import cs555.chiba.wireformats.EventFactory;
import cs555.chiba.wireformats.SampleMessage;
import cs555.chiba.wireformats.Flood;
import cs555.chiba.wireformats.RandomWalk;

public class Peer implements Node {
	private Thread icp;
	private TCPServerThread server;
	private EventFactory eventFactory;
    private TCPConnectionsCache connections;
    private int myPort;
    private InetAddress myAddr;
    private Thread serverThread;
    private UUID id;
    private LRUCache queryIDCache;

	public Peer(InetAddress registryHost, int registryPort){
		//Set-up activities - get the event factory, create the ICP
		this.id = UUID.randomUUID();
        this.eventFactory = EventFactory.getInstance(this);
        this.icp = new Thread(new InteractiveCommandParser(this));
        this.icp.start();
        queryIDCache = new LRUCache(1000);

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
        
        byte[] m = new SampleMessage(0).getBytes();
        //Send to registry
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
     * A handler for Flood messages
     * @param e The Flood message
     */
    public void Flood(Flood e){
    	if(queryIDCache.containsEntry(e.getID())) {
    		//We've already processed this query - ignore it
    		queryIDCache.putEntry(e.getID(), -1);
    		return;
    	}
    	
        System.out.println("Recieved flood message with ID: "+e.getID());
        queryIDCache.putEntry(e.getID(), -1);
        //Check if queried data is here - if so, log appropriately
        
        if(e.getCurrentHop()+1 < e.getHopLimit()) {
        	//If the message hasn't yet hit its hop limit
        	byte[] m = new Flood(e.getID(), id, e.getCurrentHop()+1, e.getHopLimit()).getBytes();
        	connections.sendAll(m, e.getSenderID());
        }
    }
    
    /**
     * A handler for RandomWalk messages
     * @param e The RandomWalk message
     */
    public void RandomWalk(RandomWalk e) {
    	if(!queryIDCache.containsEntry(e.getID())) {
    		//We've already processed this query - don't process it again (but still forward it)
    		System.out.println("Recieved random walk message with ID: "+e.getID());
    		//Check if queried data is here - if so, log appropriately
    	}
    	queryIDCache.putEntry(e.getID(), -1);
       
        if(e.getCurrentHop()+1 < e.getHopLimit()) {
        	//If the message hasn't yet hit its hop limit
        	byte[] m = new Flood(e.getID(), id, e.getCurrentHop()+1, e.getHopLimit()).getBytes();
        	connections.sendToRandom(m, e.getSenderID());
        }
    }
    
    /**
     * Routes messages to the correct handler
     * @param e The message that must be handled
     */
    @Override
    public void onEvent(Event e){
        if (e instanceof SampleMessage){ SampleMessage((SampleMessage)e); }
        else if (e instanceof Flood){ Flood((Flood)e); }
        else if (e instanceof RandomWalk){ RandomWalk((RandomWalk)e); }
    }
    
    /**
     * A method to be called by the InteractiveCommandParser
     */
    public void sampleCommand() {
    	System.out.println("This is a sample command");
    }
	
    /**
     * Creates a new Peer, then idles until exit
     * @param args The IP address of the registry and the port of the registry
     */
	public static void main(String[] args) {
		try {
			new Peer(InetAddress.getByName(args[0]), Integer.parseInt(args[1]));
		} catch (UnknownHostException e){
			System.out.println("Peer.main() " + e);
		}
        try {
        	//Idle - maybe add an "exit" command in the ICP?
            Thread.currentThread().join(); 
        } catch (InterruptedException e){ }

	}
}
