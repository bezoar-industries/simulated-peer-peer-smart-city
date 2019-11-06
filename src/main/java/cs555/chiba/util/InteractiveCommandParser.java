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
            
            if (tokens[0].equals("sample-peer-command") && node instanceof Peer){
                Peer n = (Peer)node;
                n.sampleCommand();
            } else {
            	System.out.println("Unrecognized command");
            }
        }
    }
}
