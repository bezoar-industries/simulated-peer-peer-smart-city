package cs555.chiba.overlay.network;

import cs555.chiba.iotDevices.IotDevice;
import cs555.chiba.iotDevices.IotFactory;
import cs555.chiba.iotDevices.IotTransformer;
import cs555.chiba.service.Identity;
import cs555.chiba.util.Utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author mmuller
 *
 * The Network Map builds a representation of the network for the Registry and the MessagingNodes.  When building for the Registry, it
 * creates a chain of registered nodes to prevent partitions.  It then randomly connects the other nodes together until the correct number
 * of connections is on all nodes.  Note: This approach can fail.  It's possible to create a graph where all but one node has the right number
 * of connections.  When this happens, it discards the created map and tries again.  The graphs are so small it doesn't take very long to 
 * randomly discover a working graph.
 *
 */
public class NetworkMap {

   private static final Logger logger = Logger.getLogger(NetworkMap.class.getName());

   private List<Vertex> vertices = new ArrayList<Vertex>(); // The graph vertex.  Order matters because we only want to connect two vertices once.
   private int minConnections; // min number of edges a single vertex needs to have
   private int maxConnections; // max number of edges a single vertex needs to have

   public NetworkMap(List<Identity> registeredNodes, int minConnections, int maxConnections) {
      this.minConnections = minConnections;
      this.maxConnections = maxConnections;
      verifyNumConnections(registeredNodes); // insure the graph is possible
      fillConnections(registeredNodes); // randomly create graphs until we get a good one
      verify(); // make sure we did it right
   }

   public NetworkMap(int minConnections, int maxConnections, List<Edge> edges) {
      this.minConnections = minConnections;
      this.maxConnections = maxConnections;
      addVertices(edges);
      edges.forEach(this::addEdge);
      verify(); // make sure we did it right
   }

   public NetworkMap(int numOfVertices, int minConnections, int maxConnections) {
      this.minConnections = minConnections;
      this.maxConnections = maxConnections;
      List<Identity> registeredNodes = generateNodes(numOfVertices);
      verifyNumConnections(registeredNodes); // insure the graph is possible
      fillConnections(registeredNodes); // randomly create graphs until we get a good one
      verify(); // make sure we did it right
   }

   private List<Identity> generateNodes(int numOfVertices) {
      List<Identity> rtn = new ArrayList<>();
      for (int i = 1025; i < 1025 + numOfVertices; i++) {
         rtn.add(Identity.builder().withHost("vertex").withPort(i).build());
      }
      return rtn;
   }

   /**
    *  Return the list of nodes that the given identity should initiate connections to.  This is important to ensure only
    *  one connection is created between two nodes.
    */
   public List<Identity> getConnectionList(Identity node) {
      Vertex vert = findVertex(node);
      return vert.getConnectionList();
   }

   /**
    * All the edges for recreating this map on a different node
    */
   public List<Edge> getFullEdgeList() {
      // collects a list of edge lists, then flat map stomps it into a single list of edges
      return this.vertices.stream().map(Vertex::generateEdges).collect(Collectors.toList()).stream().flatMap(List::stream).collect(Collectors.toList());
   }

   public Map<Vertex, Integer> getNeighbors(Identity identity) {
      return findVertex(identity).getEdges();
   }

   public List<Vertex> getVertices() {
      return Utilities.copy(vertices);
   }

   public int getMaxConnections() {
      return maxConnections;
   }

   public int getMinConnections() {
      return minConnections;
   }

   /**
    * Rebuild a map from a list of edges
    */
   private void addVertices(List<Edge> edges) {
      // Add all the vertices in the list.  Since everything will be listed multiple times, we'll put it in a set for uniqueness
      Set<Vertex> allVertices = new HashSet<Vertex>();
      for (Edge edge : edges) {
         allVertices.add(edge.getFirst());
         allVertices.add(edge.getSecond());
      }
      this.vertices = new ArrayList<Vertex>(allVertices);
      Collections.sort(this.vertices, Comparator.comparing(Vertex::getId));
   }

   /**
    * Build the underlying connections from the given edge
    */
   private void addEdge(Edge edge) {
      Vertex first = findVertex(edge.getFirst().getName());
      Vertex second = findVertex(edge.getSecond().getName());
      first.addEdge(second, edge.getCost());
   }

   public Vertex findVertex(Identity name) {
      return this.vertices.stream().filter(v -> v.getName().equals(name)).findFirst().get();
   }

   // defensive programming for the win! Just make sure our network is setup correctly
   private void verify() {
      if (this.vertices.stream().anyMatch(v -> v.getEdges().size() < this.minConnections || v.getEdges().size() > this.maxConnections)) {
         throw new IllegalStateException("Not all the vertices are properly connected [" + this.vertices + "]");
      }
   }

   /**
    * Since we're doing this randomly, let's make sure it is possible before we start looping.
    */
   private void verifyNumConnections(List<Identity> registeredNodes) {
      if (this.minConnections < 1) {
         throw new IllegalArgumentException("The number of overlay connections [" + this.minConnections + "] must be greater than 0.");
      }

      if (this.maxConnections == 1 && registeredNodes.size() != 2) {
         throw new IllegalArgumentException("The number of connections can only be 1 if there are only two registered nodes.  There are [" + registeredNodes.size() + "].");
      }

      if (this.maxConnections >= registeredNodes.size()) {
         throw new IllegalArgumentException("The number of overlay connections [" + this.maxConnections + "] must be less than the number of registered nodes [" + registeredNodes.size() + "].");
      }

      if (this.maxConnections < this.minConnections) {
         throw new IllegalArgumentException("The minimum number of overlay connections [" + this.minConnections + "] cannot be greater than the  [" + this.maxConnections + "]");
      }

      if ((this.minConnections * registeredNodes.size()) % 2 == 1) {
         throw new IllegalArgumentException("The min number of overlay connections [" + this.minConnections + "] times the number of registered nodes [" + registeredNodes.size() + "] must be even.");
      }
   }

   /**
    * Randomly complete the map.  If verify says it's a broken map, throw it away and try again.
    */
   private void fillConnections(List<Identity> registeredNodes) {
      boolean success = false;
      while (!success) {
         try {
            initializeLoop(registeredNodes); // connect all the vertices to ensure no partitions.
            attemptToFillConnections(); // attempt to create the graph randomly
            verify(); // check for correctness
            success = true;
         }
         catch (IllegalStateException e) {
            this.vertices = new ArrayList<>(); // we failed, clear and try again
         }
      }
   }

   /**
    * For each vertex, randomly connect to another vertex.
    */
   private void attemptToFillConnections() {
      for (Vertex vertex : this.vertices) {
         randomlyConnect(vertex);
      }
   }

   /**
    * Pick random vertices and try to connect to them.  Don't connect to myself, something I'm already
    * connected to, and something that already has the max connections
    */
   private void randomlyConnect(Vertex vertex) {
      // Attempt to randomly connect the graph.  This is not always possible, so we'll try a few times.
      for (int count = 0; !vertexCompleted(vertex, this.minConnections, count); count++) {
         Vertex edge = getRandomVertex(vertex.getName());

         if (!vertex.equals(edge) && edge.getEdges().size() < this.maxConnections && !vertex.isConnected(edge)) {
            vertex.addEdge(edge, generateCost());
         }
      }

   }

   /**
    * The Vertex is complete when it has the right number of connections.  However, this can go forever, so
    * we also keep a count.  If it has randomly try to connect to more than the max count then we just
    * move on.  Maybe some other node will connect to it to complete the vertices.  If not, then this
    * map will be thrown away.
    */
   private boolean vertexCompleted(Vertex vertex, int numConnections, int count) {
      if (vertex.getEdges().size() >= numConnections) {
         return true;
      }

      if (count >= numConnections * 10) {
         return true;
      }

      return false;
   }

   /**
    * We're not allowed to have partitions.  To make this simple, we first walk through all the vertices
    * and connect them in a loop.  You can now reach any node from any other.  The rest of the connections
    * will be filled randomly.
    */
   private void initializeLoop(List<Identity> nodes) {
      Vertex prev = null;
      for (int i = 0; i < nodes.size(); i++) {
         Vertex vertex = new Vertex(i, nodes.get(i), generateIotDevices());
         this.vertices.add(vertex);

         if (prev != null) {
            vertex.addEdge(prev, generateCost());
         }
         prev = vertex;
      }

      // if there are only 2 nodes, don't double connect
      if (!prev.isConnected(this.vertices.get(0))) {
         prev.addEdge(this.vertices.get(0), generateCost()); // complete the circle by linking the last to the first.
      }
   }

   /**
    * Random cost between 1 and 10.
    */
   private int generateCost() {
      return ThreadLocalRandom.current().nextInt(1, 11);
   }

   /**
    * Random iot device string
    */
   private String generateIotDevices() {
      List<IotDevice> connectedIotDevices = IotFactory.generateRandomDevices(3, 30);
      IotTransformer trans = new IotTransformer(connectedIotDevices);
      return trans.getDeviceString();
   }

   @Override public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((vertices == null) ? 0 : vertices.hashCode());
      return result;
   }

   @Override public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      NetworkMap other = (NetworkMap) obj;
      if (vertices == null) {
         return other.vertices == null;
      }
      else
         return vertices.equals(other.vertices);
   }

   @Override public String toString() {
      return "NetworkMap [vertices=" + vertices + "]";
   }

   // get a random vertext that isn't 'this' one.
   public Vertex getRandomVertex(Identity ident) {
      Vertex rtn = null;
      while (rtn == null) {
         int index = ThreadLocalRandom.current().nextInt(0, this.vertices.size());
         Vertex vert = this.vertices.get(index);
         if (!vert.getName().equals(ident)) {
            rtn = vert;
         }
      }
      return rtn;
   }

   public int size() {
      return this.vertices.size();
   }
}
