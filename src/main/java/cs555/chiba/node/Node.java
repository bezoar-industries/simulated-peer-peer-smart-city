/**
* The Event interface is implemented by all nodes
* and declares the onEvent method.
*
* @author  Kevin Bruhwiler
* @version 1.0
* @since   2019-06-11 
*/
package cs555.chiba.node;
import cs555.chiba.wireformats.Event;


public interface Node {
    void onEvent(Event e);
}