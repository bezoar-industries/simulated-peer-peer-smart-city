<h1 align="center">Decentralized Data Retrieval and Analysis in an Unstructured Peer-to-Peer Smart-City Simulation</h1>

<p align="center">
  <b>Authors</b><br>
  Joseph Larson |
  Kevin Bruhwiler |
  Matt Muller
</p>

## Environment
* Install Java jdk 1.8+
* Install Gradle 5+
* Add the main directory to the classpath in .bashrc
  * export CLASSPATH=".:./build/classes/java/main:./lib/*"
  * source .bashrc
* Untar java bundle or clone the repo
* In the bundle build the project
  * gradle assemble

## Simple Network
To get started, create a small network of 1 registry and 3 peers.

* Start the Registry
  * java -Djava.util.logging.config.class=cs555.chiba.service.LogConfig cs555.chiba.registry.RegistryNode PORT
  * Example: java -Djava.util.logging.config.class=cs555.chiba.service.LogConfig cs555.chiba.registry.RegistryNode 60000

* Start a small network of 3 peers by running the below command in 3 different terminals.
  * java -Xmx5M -Djava.util.logging.config.class=cs555.chiba.service.LogConfig -Dcsu.log.file=$LOGFILE cs555.chiba.node.Peer REGISTRY PORT IOT_COUNT GOSSIP_CACHE_SIZE
  * Example: java -Xmx20M -Djava.util.logging.config.class=cs555.chiba.service.LogConfig -Dcsu.log.file=$LOGFILE cs555.chiba.node.Peer 192.168.100.2 60000 0 40

Now that all the peers are running, we need to build our network.  From the Registry issue these commands.
* Generate our network with a min and max number of connections per peer
  * Registry->buildoverlay 2 2

* Push the generated network to the peers.
  * Registry->connectpeers

* Export overlay to a file for future use
  * Registry->exportoverlay savedoverlay.csv

You're now ready to use your small network!

* The Registry will tell all the peers to shut down when you are done.
  * Registry->shutdown

* Individual peers can be shutdown as well
  * Peer->exit

## Larger Networks

To run networks of thousands of peers, it's not practical to start them up individually.  We've provided scripts to help.
* Start your Registry as you did before.
  * java -Djava.util.logging.config.class=cs555.chiba.service.LogConfig cs555.chiba.registry.RegistryNode PORT

* Launch a large number of peers on a machine with the lotsofpeers.sh script
  * Edit the lotsofpeers.sh script with the IP and Port of your Registry
  * Run lotsofpeers.sh
    * lotsofpeers.sh NUMBER_OF_PEERS GOSSIP_CACHE_SIZE
    * Example: lotsofpeers.sh 100 40

* To launch peers on multiple machines, use the startupNoShell.sh script
  * startupNoShell.sh TEXT_FILE_OF_MACHINES
  * Example: startupNoShell.sh workers.txt

* To shut them down use the Registry Shutdown command
  * Registry->shutdown
* To ensure they all turn off, use the shutdown script
  * shutdown.sh workers.txt
* If something went wrong, use the angry shutdown script
  * angryshutdown.sh workers.txt


## Running a Search
TODO

## Performance Data
TODO

## Registry Commands

### help
List available commands

### exit
Shutdown this node

### name
What is the identity of this node

### listconnections
List all nodes with active connections to this node.  This includes everything connected to the peer.

### listpeers
List all the registered peers.

### randomWalk <metric> <hop limit>
Initiate a random walk experiment.

### flood <metric> <hop limit>
Initiate a flood experiment.

### gossiptype0 <metric> <hop limit>
Initiate a Gossip experiment using distance.

### gossiptype1 <metric> <hop limit>
Initiate a Gossip experiment using cached types.

### buildoverlay <min> <max>
Build a random overlay with min and maximum number of connections.

Note, the overlay builder will attempt to create graphs until it succeeds to create one based on the perscribed min/max connections per peer.  If you are overly strict, this could take a long time.

### exportoverlay <file.csv>
Export the overlay to a file.

### importoverlay <file.csv>
Read in an overlay using the currently registered peers for the vertices.

### connectpeers
Connect all the peers in the manner prescribed by the overlay.

### shutdown
Tell all the peers to shutdown and close their sockets.

### print-results
Print the results to the terminal.
      
### export-results <file.csv>
Write results to a file.

### clear-results
Clear the results to start a new experiment.
      
### checkpeers
Verify the peers are connected as described in the overlay.

### exportgephi <file.csv>
Export the overlay into the gephi format for visualizations.

## Peer Commands

### help
List available commands.

### exit
Shutdown this node.

### name
Print the identity of this node.

### listconnections
List all nodes with active connections to this node.  This includes everything connected to the peer.

### listpeers
List all neighboring peers.  These are the connections the search algorithms can see and use.  The Registry and other support style connections will be excluded.

### listdevices
List the IOT devices on this peer.
      
### gossipdata
List the cached gossip distance entries on this peer.

### gossipentries
List the cached gossip entry locations on this peer.

### querymetrics
List the gathered metrics on this peer.

## Logging

Logging works with the basic Java Logging Platform.  Levels can be configured in cs555.chiba.service.LogConfig.

The log file is specified during startup of the nodes like so:

java -Djava.util.logging.config.class=cs555.chiba.service.LogConfig -Dcsu.log.file=logs/csuServer.log cs555.chiba.registry.RegistryNode 60000

The system will not create any subdirectories, so you will need to create the 'logs' directory in the above example.

The server will use the same log file every time you start up a Registry.

For peers:

java -Xmx20M -Djava.util.logging.config.class=cs555.chiba.service.LogConfig -Dcsu.log.file=logs/csuPeer.log cs555.chiba.node.Peer 192.168.100.2 60000 0 40

To avoid interleaving peer data, the config will detect the word 'peer' in the log file name.  If it finds 'peer' it will append the startup time to the filename.  This will give each peer their own log.








