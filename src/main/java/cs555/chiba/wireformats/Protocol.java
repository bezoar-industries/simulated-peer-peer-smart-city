/**
* The Protocol class simply defines the ID for all
* different message types (each ID must be unique)
*
* @author  Kevin Bruhwiler
* @version 1.0
* @since   2019-06-11 
*/

package cs555.chiba.wireformats;


class Protocol {
    public static final int SAMPLE_MESSAGE = 1;
    public static final int FLOOD = 2;
    public static final int RANDOM_WALK = 3;
    public static final int GOSSIP_DATA = 4;
    public static final int GOSSIP_QUERY = 5;
}
