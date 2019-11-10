package cs555.chiba.overlay.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author mmuller
 *
 * Represents a vertex on a graph.  The Identity names the node, and the edges are all the connections leading off the node.  The id gives an arbitrary order
 * so that we don't duplicate connections.  Basically connections are always initiated from the lower id.
 */
public class Vertex {

   private final int id; // the ordered id used ensure we don't repeat our messages to the nodes
   private final Identity name; // the message node this vertex represents
   private Map<Vertex, Integer> edges; // the cost of traversing an edge

   public Vertex(int id, Identity name) {
      this.id = id;
      this.name = name;
      this.edges = new HashMap<Vertex, Integer>();
   }

   public int getId() {
      return id;
   }

   public Identity getName() {
      return name;
   }

   public Map<Vertex, Integer> getEdges() {
      return this.edges;
   }

   /**
    * Is this vertext connected to the other?
    */
   public boolean isConnected(Vertex edge) {
      // check both sides in case the addEdge function failed to properly connect the vertices
      return this.edges.get(edge) != null || edge.getEdges().get(this) != null;
   }

   public void addEdge(Vertex edge, int cost) {
      // make sure we aren't already connected
      if (isConnected(edge)) {
         throw new IllegalArgumentException("The supplied vertex [" + edge + "] is already connected to this vertex [" + this + "]");
      }

      // make sure we aren't connecting to ourselves
      if (this.equals(edge)) {
         throw new IllegalArgumentException("Attempting to connect to myself [" + edge + "]");
      }

      this.edges.put(edge, cost);
      edge.getEdges().put(this, cost); // add the connection to the other side
   }

   public List<Identity> getConnectionList() {
      // try not to leak the connection set
      return this.edges.keySet().stream().filter(v -> v.getId() > this.getId()).map(Vertex::getName).collect(Collectors.toList());
   }

   /**
    * Create a list of edges that initiate from this vertex.  The lower id initiates the connection.  This means the Vertex with
    * the highest ID doesn't initiate connections to any other vertex.
    */
   public List<Edge> generateEdges() {
      List<Vertex> verts = this.edges.keySet().stream().filter(v -> v.getId() > this.getId()).collect(Collectors.toList());

      List<Edge> rtn = new ArrayList<Edge>();
      for (Vertex vert : verts) {
         Edge edge = Edge.builder().withFirst(this).withSecond(vert).withCost(this.edges.get(vert)).build();
         rtn.add(edge);
      }

      return rtn;
   }

   public Integer getCost(Vertex vert) {
      return this.edges.get(vert);
   }

   // can't use edges in the equals because it links back to itself
   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + id;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
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
      Vertex other = (Vertex) obj;
      if (id != other.id)
         return false;
      if (name == null) {
         if (other.name != null)
            return false;
      }
      else if (!name.equals(other.name))
         return false;
      return true;
   }

   @Override
   public String toString() {
      return "Vertex [id=" + id + ", name=" + name + ", edges=" + edges.keySet().stream().map(v -> v.getName().getHost()).collect(Collectors.joining(",")) + "]";
   }

}
