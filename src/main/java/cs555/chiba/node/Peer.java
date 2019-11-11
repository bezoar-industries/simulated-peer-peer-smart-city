/**
* The Peer class is the entry point for individual peers
* and manages setup and event handling.
*
* @author  Kevin Bruhwiler
* @version 1.0
* @since   2019-06-11 
*/

package cs555.chiba.node;

import cs555.chiba.iotDevices.*;
import cs555.chiba.service.Identity;
import cs555.chiba.transport.TCPConnectionsCache;
import cs555.chiba.transport.TCPReceiverThread;
import cs555.chiba.transport.TCPSender;
import cs555.chiba.transport.TCPServerThread;
import cs555.chiba.util.InteractiveCommandParser;
import cs555.chiba.util.Utilities;
import cs555.chiba.wireformats.Event;
import cs555.chiba.wireformats.EventFactory;
import cs555.chiba.wireformats.SampleMessage;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Peer implements Node {
   private static final Logger logger = Logger.getLogger(Peer.class.getName());

   private Thread icp;
	private TCPServerThread server;
	private EventFactory eventFactory;
    private TCPConnectionsCache connections;
    private int myPort;
    private InetAddress myAddr;
    private Thread serverThread;
    private int id;
    private List<IotDevice> connectedIotDevices;

	public Peer(InetAddress registryHost, int registryPort, int id, int numberOfIoTDevices){
		//Set-up activities - get the event factory, create the ICP
        this.eventFactory = EventFactory.getInstance(this);
        this.createIotNetwork(numberOfIoTDevices);

        this.icp = new Thread(new InteractiveCommandParser(this));
        this.icp.start();


      Identity whoAmI = Identity.builder().withHost(registryHost.getHostName()).withPort(registryPort).build();

        try {
            //Connect to registry and start thread
        	Socket registrySocket = new Socket(registryHost, registryPort);
            //add registry to connections cache - we will want to always keep this connection open
            this.connections = new TCPConnectionsCache(new TCPSender(registrySocket), whoAmI);
            Thread registryThread = new Thread(new TCPReceiverThread(registrySocket, eventFactory));
            this.connections.addReceiverThread(registryThread);
            registryThread.start();
        } catch (IOException e){
           logger.log(Level.SEVERE, "Peer() ", e);
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

    private void createIotNetwork(int numberOfIoTDevices) {
        this.connectedIotDevices = new LinkedList<>();
        if(numberOfIoTDevices == 0) {
            // if no number of IoT devices defined, then generate a random number between 3 and 30 devices
            // Upper bounds of 27 and then adding 3 to ensure the range above is followed
            Random random = new Random();
            numberOfIoTDevices = random.nextInt(27) + 3;
        }

        System.out.println("number of devices: " + numberOfIoTDevices);

        // Add devices to our array list
        for(int i = 0; i < numberOfIoTDevices; i++) {

            // Change this int when adding a new IoT device
            int numberOfPossibleIotDevices = 19;
            Random random = new Random();
            int possibleDevicesRandomIndex = random.nextInt(numberOfPossibleIotDevices);
            switch(possibleDevicesRandomIndex){
                case 0 :
                    this.connectedIotDevices.add(new Thermometer());
                    break;
                case 1 :
                    this.connectedIotDevices.add(new Thermostat());
                    break;
                case 2 :
                    this.connectedIotDevices.add(new DoorLock());
                    break;
                case 3 :
                    this.connectedIotDevices.add(new Outlet());
                    break;
                case 4 :
                    this.connectedIotDevices.add(new AirPollutionMonitor());
                    break;
                case 5 :
                    this.connectedIotDevices.add(new AirVent());
                    break;
                case 6 :
                    this.connectedIotDevices.add(new DoorSensor());
                    break;
                case 7 :
                    this.connectedIotDevices.add(new Dryer());
                    break;
                case 8 :
                    this.connectedIotDevices.add(new LightSwitch());
                    break;
                case 9 :
                    this.connectedIotDevices.add(new Microwave());
                    break;
                case 10 :
                    this.connectedIotDevices.add(new Refrigerator());
                    break;
                case 11 :
                    this.connectedIotDevices.add(new TV());
                    break;
                case 12 :
                    this.connectedIotDevices.add(new WashMachine());
                    break;
                case 13 :
                    this.connectedIotDevices.add(new Watch());
                    break;
                case 14 :
                    this.connectedIotDevices.add(new WaterLeakSensor());
                    break;
                case 15 :
                    this.connectedIotDevices.add(new WindowSensor());
                    break;
                case 16 :
                    this.connectedIotDevices.add(new Clock());
                    break;
                case 17 :
                    this.connectedIotDevices.add(new StreetLight());
                    break;
                case 18 :
                    this.connectedIotDevices.add(new PowerMeter());
                    break;
            }
        }
        System.out.println(this.connectedIotDevices);
        System.out.println("Number of actual devices: " + this.connectedIotDevices.size());
    }

    private Integer calculateTotalDevicesWithMetric(String metricName) {
	    int devicesWithMetric = 0;
	    for (IotDevice device : this.connectedIotDevices) {
	        devicesWithMetric += device.getMetric(metricName);
        }
        return devicesWithMetric;
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
        logger.info("Recieved sample message with num: "+e.getNum());
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
       logger.info("This is a sample command");
    }
	
    /**
     * Creates a new Peer, then idles until exit
     * @param args The IP address of the registry, the port of the registry, and this Peer's ID
     */
	public static void main(String[] args) {
		try {
         parseArguments(args);
		} catch (UnknownHostException e){
			logger.log(Level.SEVERE, "Peer.main() ", e);
		}
        try {
        	//Idle - maybe add an "exit" command in the ICP?
            Thread.currentThread().join();
        } catch (InterruptedException e){ }

	}

   private static Peer parseArguments(String[] args) throws UnknownHostException {

      if (!Utilities.checkArgCount(4, args)) {
         throw new IllegalArgumentException("Peer Node requires 4 arguments:  registry-host registry-port peer-id iot-count");
      }

      InetAddress addr = InetAddress.getByName(args[0]);
      int port = Utilities.parsePort(args[1]);
      Identity registry = Identity.builder().withHost(InetAddress.getLocalHost().getHostAddress()).withPort(port).build();
      int id = Integer.parseInt(args[2]);
      int iotCout = Integer.parseInt(args[3]);

      return new Peer(addr, port, id, iotCout);
   }
}
