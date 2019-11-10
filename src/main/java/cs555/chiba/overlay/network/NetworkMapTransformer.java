package cs555.chiba.overlay.network;

import cs555.chiba.util.Utilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NetworkMapTransformer {

   private static final Logger logger = Logger.getLogger(NetworkMapTransformer.class.getName());

   private NetworkMap netMap;

   public NetworkMapTransformer(NetworkMap netMap) {
      this.netMap = netMap;
   }

   public NetworkMapTransformer(File csvData) throws IOException {
      String data = new String(Utilities.readFile(csvData));
      parseData(data);
   }

   public NetworkMap applyRegisteredNodes(List<Identity> registeredNodes) {
      if (registeredNodes.size() != this.netMap.size()) {
         throw new IllegalArgumentException("Attempted to recreate overlay with too few nodes [" + registeredNodes.size() + "].  This overlay requires [" + this.netMap.size() + "] nodes.");
      }

      Map<Vertex, Vertex> assignments = createMapping(this.netMap.getVertices(), registeredNodes);
      return buildNetwork(this.netMap.getFullEdgeList(), assignments);
   }

   private Map<Vertex, Vertex> createMapping(List<Vertex> vertices, List<Identity> registeredNodes) {
      Map<Vertex, Vertex> assignments = new HashMap<>();
      for (int i = 0; i < vertices.size(); i++) {
         Vertex ver = new Vertex(vertices.get(i).getId(), registeredNodes.get(i));
         assignments.put(vertices.get(i), ver);
      }

      return assignments;
   }

   private NetworkMap buildNetwork(List<Edge> fullEdgeList, Map<Vertex, Vertex> assignments) {
      List<Edge> edges = buildEdgeList(fullEdgeList, assignments);
      return new NetworkMap(this.netMap.getMinConnections(), this.netMap.getMaxConnections(), edges);
   }

   private List<Edge> buildEdgeList(List<Edge> fullEdgeList, Map<Vertex, Vertex> assignments) {
      List<Edge> edges = new ArrayList<>();
      fullEdgeList.forEach(edge -> {
         Vertex first = assignments.get(edge.getFirst());
         Vertex second = assignments.get(edge.getSecond());
         edges.add(new Edge(first, second, edge.getCost()));
      });
      return edges;
   }

   public String export() {
      String out = this.netMap.getMinConnections() + "," + this.netMap.getMaxConnections() + "\n";
      return out + this.netMap.getFullEdgeList().stream().map(Edge::toCsv).collect(Collectors.joining("\n"));
   }

   private void parseData(String data) {
      String[] rows = data.split("\n");
      String[] connections = rows[0].split(",");
      List<Edge> edges = Stream.of(rows).skip(1).map(this::createEdge).collect(Collectors.toList());
      this.netMap = new NetworkMap(Utilities.quietlyParseInt(connections[0], 1), Utilities.quietlyParseInt(connections[1], 1), edges);
   }

   private Edge createEdge(String row) {
      // Thanks https://stackoverflow.com/questions/1757065/java-splitting-a-comma-separated-string-but-ignoring-commas-in-quotes for this regex
      String[] columns = row.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
      return Edge.fromCsv(columns);
   }

   public NetworkMap getNetworkMap() {
      return this.netMap;
   }
}
