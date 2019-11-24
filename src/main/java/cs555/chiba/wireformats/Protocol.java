/**
 * The Protocol class simply defines the ID for all
 * different message types (each ID must be unique)
 *
 * @author Kevin Bruhwiler
 * @version 1.0
 * @since 2019-06-11
 */

package cs555.chiba.wireformats;

enum Protocol {
   FLOOD, RANDOM_WALK, GOSSIP_DATA, GOSSIP_ENTRIES, GOSSIP_QUERY, INTRODUCTION, REGISTER, INITIATE_CONNECTIONS, SHUTDOWN, LIST_PEERS_REQUEST, LIST_PEERS_RESPONSE
}
