/**
* The InteractiveCommandParser reads text from the console
* and calls functions within its node.
*
* @author  Kevin Bruhwiler
* @version 1.0
* @since   2019-06-11 
*/

package cs555.chiba.util;

import java.util.Scanner;

import cs555.chiba.node.Peer;
import cs555.chiba.node.Node;
import cs555.chiba.registry.RegistryNode;


public class InteractiveCommandParser implements Runnable{
    private Scanner sc = new Scanner(System.in);
    private Node node;

    public InteractiveCommandParser(Node node){
        this.node = node;
    }

    /**
     * Blocks while attempting to read text from the console
     */
    public void run(){
        while(sc.hasNext()){
            String str = sc.nextLine();
            String[] tokens = str.split(" ");

            if(node instanceof Peer) {
                if (tokens[0].equals("sample-peer-command")){
                    Peer n = (Peer)node;
                    n.sampleCommand();
                } else {
                    System.out.println("Unrecognized command");
                }
            } else if (node instanceof RegistryNode) {
                if (tokens[0].equals("random-walk") && tokens.length == 2){
                    RegistryNode n = (RegistryNode)node;
                    n.sendRandomWalkRequest(tokens[1]);
                } else if (tokens[0].equals("gossip") && tokens.length == 2){
                    RegistryNode n = (RegistryNode)node;
                    n.sendGossipingRequest(tokens[1]);
                } else if (tokens[0].equals("flood") && tokens.length == 2){
                    RegistryNode n = (RegistryNode)node;
                    n.sendFloodingRequest(tokens[1]);
                } else if (tokens[0].equals("deep-learning") && tokens.length == 2){
                    RegistryNode n = (RegistryNode)node;
                    n.sendDeepLearningRequest(tokens[1]);
                } else {
                    System.out.println("Unrecognized command");
                }
            }

        }
    }
}
