package cs555.chiba.overlay.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
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
 * When building a map for a MessagingNode, the edges are supplied by the LinkWeights message.  It builds the map back up according to
 * the instructions provided by the list of Edges.
 */
public class NetworkMap {

   private List<Vertex> vertices = new ArrayList<Vertex>(); // The graph vertex.  Order matters because we only want to connect two vertices once.
   private int numConnections; // the number of edges a single vertex needs to have

   public NetworkMap(List<Identity> registeredNodes, int numConnections) {
      this.numConnections = numConnections;
      verifyNumConnections(registeredNodes); // insure the graph is possible
      fillConnections(registeredNodes, numConnections); // randomly create graphs until we get a good one
      verify(); // make sure we did it right
   }

   public NetworkMap(int numConnections, List<Edge> edges) {
      this.numConnections = numConnections;
      addVertices(edges);
      edges.stream().forEach(this::addEdge);
      verify(); // make sure we did it right
   }

   /**
    *  Return the list of nodes that the given identity should initiate connections to.  This is importat to ensure only 
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

   public int getNumConnections() {
      return this.numConnections;
   }

   public Map<Vertex, Integer> getNeighbors(Identity identity) {
      return findVertex(identity).getEdges();
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
      if (this.vertices.stream().anyMatch(v -> v.getEdges().size() != this.numConnections)) {
         throw new IllegalStateException("Not all the vertices are properly connected [" + this.vertices + "]");
      }
   }

   /**
    * Since we're doing this randomly, let's make sure it is possible before we start looping.
    */
   private void verifyNumConnections(List<Identity> registeredNodes) {
      if (this.numConnections < 1) {
         throw new IllegalArgumentException("The number of overlay connections [" + this.numConnections + "] must be greater than 0.");
      }

      if (this.numConnections == 1 && registeredNodes.size() != 2) {
         throw new IllegalArgumentException("The number of connections can only be 1 if there are only two registered nodes.  There are [" + registeredNodes.size() + "].");
      }

      if (this.numConnections >= registeredNodes.size()) {
         throw new IllegalArgumentException("The number of overlay connections [" + this.numConnections + "] must be less than the number of registered nodes [" + registeredNodes.size() + "].");
      }

      if ((this.numConnections * registeredNodes.size()) % 2 == 1) {
         throw new IllegalArgumentException("The number of overlay connections [" + this.numConnections + "] times the number of registered nodes [" + registeredNodes.size() + "] must be even.");
      }
   }

   /**
    * Randomly complete the map.  If verify says it's a broken map, throw it away and try again.
    */
   private void fillConnections(List<Identity> registeredNodes, int numConnections) {
      boolean success = false;
      while (!success) {
         try {
            intializeLoop(registeredNodes); // connect all the vertices to ensure no partitions.
            attemptToFillConnections(numConnections); // attempt to create the graph randomly
            verify(); // check for correctness
            success = true;
         }
         catch (IllegalStateException e) {
            this.vertices = new ArrayList<Vertex>(); // we failed, clear and try again
         }
      }
   }

   /**
    * For each vertex, randomly connect to another vertex.
    */
   private void attemptToFillConnections(int numConnections) {
      for (Vertex vertex : this.vertices) {
         randomlyConnect(vertex, numConnections);
      }
   }

   /**
    * Pick random vertices and try to connect to them.  Don't connect to myself, something I'm already connected to, and something that has fewer
    * than the required number of connections.
    */
   private void randomlyConnect(Vertex vertex, int numConnections) {
      // Attempt to randomly connect the graph.  This is not always possible, so we'll try a few times.
      for (int count = 0; !vertexCompleted(vertex, numConnections, count); count++) {
         Vertex edge = getRandomVertex(vertex.getName());

         if (!vertex.equals(edge) && edge.getEdges().size() < numConnections && !vertex.isConnected(edge)) {
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

      if (count >= numConnections * this.vertices.size()) {
         return true;
      }

      return false;
   }

   /**
    * We're not allowed to have partitions.  To make this simple, we first walk through all the vertices
    * and connect them in a loop.  You can now reach any node from any other.  The rest of the connections
    * will be filled randomly.
    */
   private void intializeLoop(List<Identity> nodes) {
      Vertex prev = null;
      for (int i = 0; i < nodes.size(); i++) {
         Vertex vertex = new Vertex(i, nodes.get(i));
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

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((vertices == null) ? 0 : vertices.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      NetworkMap other = (NetworkMap) obj;
      if (vertices == null) {
         if (other.vertices != null)
            return false;
      }
      else if (!vertices.equals(other.vertices))
         return false;
      return true;
   }

   @Override
   public String toString() {
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

}
