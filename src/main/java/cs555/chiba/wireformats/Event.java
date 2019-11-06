/**
* The Event interface is implemented by all message types
* and is used to pass all messages to the peers.
*
* @author  Kevin Bruhwiler
* @version 1.0
* @since   2019-06-11 
*/
package cs555.chiba.wireformats;


public interface Event {
    byte[] getBytes();

    int getType();
}