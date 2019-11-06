package cs555.chiba.overlay.network;

import cs555.chiba.util.Utilities;

/**
 * @author mmuller
 *
 * Simple representation of a connection or path between two vertices of a graph.  In our case, these are messaging nodes.
 */
public class Edge {

   private final Vertex first; // vertex #1.  This is not a directed graph.
   private final Vertex second; // vertex #2.  This is not a directed graph.
   private final Integer cost; // cost of moving from one vertex to the other

   Edge(Vertex first, Vertex second, Integer cost) {
      this.first = first;
      this.second = second;
      this.cost = cost;
   }

   public Vertex getFirst() {
      return this.first;
   }

   public Vertex getSecond() {
      return this.second;
   }

   public Integer getCost() {
      return this.cost;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((cost == null) ? 0 : cost.hashCode());
      result = prime * result + ((first == null) ? 0 : first.hashCode());
      result = prime * result + ((second == null) ? 0 : second.hashCode());
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
      Edge other = (Edge) obj;
      if (cost == null) {
         if (other.cost != null)
            return false;
      }
      else if (!cost.equals(other.cost))
         return false;
      if (first == null) {
         if (other.first != null)
            return false;
      }
      else if (!first.equals(other.first))
         return false;
      if (second == null) {
         if (other.second != null)
            return false;
      }
      else if (!second.equals(other.second))
         return false;
      return true;
   }

   @Override
   public String toString() {
      return "Edge [first=" + first + ", second=" + second + ", cost=" + cost + "]";
   }

   public String printEdge() {
      String path = this.getFirst().getName().getIdentityKey();
      path += " " + this.getSecond().getName().getIdentityKey();
      path += " " + this.getCost();
      return path;
   }

   public static Builder builder() {
      return new Builder();
   }

   public static Builder builder(Edge edge) {
      return new Builder(edge);
   }

   public static final class Builder {

      private Vertex first;
      private Vertex second;
      private Integer cost;

      private Builder() {
      }

      private Builder(Edge edge) {
         if (edge != null) {
            withFirst(edge.getFirst());
            withSecond(edge.getSecond());
            withCost(edge.getCost());
         }
      }

      public Builder withFirst(Vertex first) {
         this.first = first;
         return this;
      }

      public Builder withSecond(Vertex second) {
         this.second = second;
         return this;
      }

      public Builder withCost(Integer cost) {
         this.cost = cost;
         return this;
      }

      public Edge build() {
         // the second vertex can be null if it's the last in a path
         Utilities.checkArgument(this.first != null, "First vertex cannot be null");
         Utilities.checkArgument(this.cost != null, "Cost of edge cannot be null");

         return new Edge(this.first, this.second, this.cost);
      }
   }
}
