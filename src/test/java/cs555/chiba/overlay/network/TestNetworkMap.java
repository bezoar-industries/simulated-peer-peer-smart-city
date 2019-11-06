package cs555.chiba.overlay.network;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class TestNetworkMap {

   @Test(expected = IllegalArgumentException.class)
   public void testNetworkMapCreation() {
      List<Identity> nodes = createRegisteredNodes();
      nodes.add(createIdentity("nowhere"));

      NetworkMap net = new NetworkMap(nodes, 1);
   }

   @Test(expected = IllegalArgumentException.class)
   public void testNetworkMapOddNumberCreation() {
      NetworkMap net = new NetworkMap(createRegisteredNodes(), 3);
   }

   @Test(expected = IllegalArgumentException.class)
   public void testNetworkMapMaxCreation() {
      NetworkMap net = new NetworkMap(createRegisteredNodes(), 9);
   }

   @Test(expected = IllegalArgumentException.class)
   public void testNetworkMapMoreThanMaxCreation() {
      NetworkMap net = new NetworkMap(createRegisteredNodes(), 10);
   }

   @Test
   public void testNetworkLotsOfConnectionsMapCreation() {
      NetworkMap net = new NetworkMap(createRegisteredNodes(), 6);
   }

   @Test
   public void testNetworkMaxLegalConnectionsMapCreation() {
      NetworkMap net = new NetworkMap(createRegisteredNodes(), 8);
   }

   @Test
   public void testNetworkTwoConnectionsMapCreation() {
      List<Identity> idents = Arrays.asList(createIdentity("albany"), createIdentity("annapolis"));
      NetworkMap net = new NetworkMap(new ArrayList<Identity>(idents), 1);
   }

   public List<Identity> createRegisteredNodes() {
      //@formatter:off
      List<Identity> idents = Arrays.asList(
            createIdentity("albany"), 
            createIdentity("annapolis"), 
            createIdentity("atlanta"), 
            createIdentity("augusta"), 
            createIdentity("austin"), 
            createIdentity("baton-rouge"), 
            createIdentity("bismarck"), 
            createIdentity("boise"), 
            createIdentity("boston")); 
      //@formatter:on
      return new ArrayList<Identity>(idents);
   }

   public Identity createIdentity(String host) {
      return Identity.builder().withHost(host).withPort(randomPort()).build();
   }

   private int randomPort() {
      return ThreadLocalRandom.current().nextInt(1024, 65536);
   }

   public List<Vertex> createVerticesList() {
      List<Vertex> vertices = new ArrayList<Vertex>();

      vertices.add(new Vertex(0, createIdentity("albany")));
      vertices.add(new Vertex(1, createIdentity("annapolis")));
      vertices.add(new Vertex(2, createIdentity("atlanta")));
      vertices.add(new Vertex(3, createIdentity("augusta")));
      vertices.add(new Vertex(4, createIdentity("austin")));
      vertices.add(new Vertex(5, createIdentity("baton-rouge")));
      vertices.add(new Vertex(6, createIdentity("bismarck")));
      vertices.add(new Vertex(7, createIdentity("boise")));
      vertices.add(new Vertex(8, createIdentity("boston")));
      vertices.add(new Vertex(9, createIdentity("carson-city")));

      return vertices;
   }

   public NetworkMap buildManualNetwork(List<Vertex> vertices) {
      Vertex albany = vertices.get(0);
      Vertex annapolis = vertices.get(1);
      Vertex atlanta = vertices.get(2);
      Vertex augusta = vertices.get(3);
      Vertex austin = vertices.get(4);
      Vertex batonRouge = vertices.get(5);
      Vertex bismarck = vertices.get(6);
      Vertex boise = vertices.get(7);
      Vertex boston = vertices.get(8);
      Vertex carsonCity = vertices.get(9);

      List<Edge> edges = new ArrayList<Edge>();

      // 0
      edges.add(new Edge(albany, annapolis, 9)); // 1
      edges.add(new Edge(albany, batonRouge, 5)); // 5
      edges.add(new Edge(albany, carsonCity, 1)); // 9

      // 1
      edges.add(new Edge(annapolis, atlanta, 6)); // 2
      edges.add(new Edge(annapolis, bismarck, 2)); // 6

      // 2
      edges.add(new Edge(atlanta, augusta, 7)); // 3
      edges.add(new Edge(atlanta, boise, 3)); // 7

      // 3
      edges.add(new Edge(augusta, austin, 8)); // 4
      edges.add(new Edge(augusta, boston, 4)); // 8

      // 4
      edges.add(new Edge(austin, batonRouge, 9)); // 5
      edges.add(new Edge(austin, carsonCity, 5)); // 9

      // 5
      edges.add(new Edge(batonRouge, bismarck, 6)); // 6

      // 6
      edges.add(new Edge(bismarck, boise, 7)); // 7

      // 7
      edges.add(new Edge(boise, boston, 8)); // 8

      //8
      edges.add(new Edge(boston, carsonCity, 9)); // 9

      return new NetworkMap(3, edges);
   }

   @Test
   public void testConnectionList() {
      List<Vertex> vertices = createVerticesList();
      NetworkMap net = buildManualNetwork(vertices);
      verifyManualNetworkMap(vertices, net);
   }

   public void verifyManualNetworkMap(List<Vertex> vertices, NetworkMap net) {
      List<Identity> albany = net.getConnectionList(vertices.get(0).getName());
      assertEquals(3, albany.size());
      assertTrue(albany.contains(vertices.get(1).getName()));
      assertTrue(albany.contains(vertices.get(5).getName()));
      assertTrue(albany.contains(vertices.get(9).getName()));

      List<Identity> annapolis = net.getConnectionList(vertices.get(1).getName());
      assertEquals(2, annapolis.size());
      assertTrue(annapolis.contains(vertices.get(2).getName()));
      assertTrue(annapolis.contains(vertices.get(6).getName()));

      List<Identity> atlanta = net.getConnectionList(vertices.get(2).getName());
      assertEquals(2, atlanta.size());
      assertTrue(atlanta.contains(vertices.get(3).getName()));
      assertTrue(atlanta.contains(vertices.get(7).getName()));

      List<Identity> augusta = net.getConnectionList(vertices.get(3).getName());
      assertEquals(2, augusta.size());
      assertTrue(augusta.contains(vertices.get(4).getName()));
      assertTrue(augusta.contains(vertices.get(8).getName()));

      List<Identity> austin = net.getConnectionList(vertices.get(4).getName());
      assertEquals(2, austin.size());
      assertTrue(austin.contains(vertices.get(5).getName()));
      assertTrue(austin.contains(vertices.get(9).getName()));

      List<Identity> batonRouge = net.getConnectionList(vertices.get(5).getName());
      assertEquals(1, batonRouge.size());
      assertTrue(batonRouge.contains(vertices.get(6).getName()));

      List<Identity> bismarck = net.getConnectionList(vertices.get(6).getName());
      assertEquals(1, bismarck.size());
      assertTrue(bismarck.contains(vertices.get(7).getName()));

      List<Identity> boise = net.getConnectionList(vertices.get(7).getName());
      assertEquals(1, boise.size());
      assertTrue(boise.contains(vertices.get(8).getName()));

      List<Identity> boston = net.getConnectionList(vertices.get(8).getName());
      assertEquals(1, boston.size());
      assertTrue(boston.contains(vertices.get(9).getName()));

      List<Identity> carsonCity = net.getConnectionList(vertices.get(9).getName());
      assertEquals(0, carsonCity.size());
   }
}
