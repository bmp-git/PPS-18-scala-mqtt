package mqtt.broker

import mqtt.broker.state.{Channel, State}
import mqtt.model.Packet

/**
 * Represents the logic to modify the server state at the moment of a packet reception.
 */
trait ProtocolManager {
  /**
   * Handles a packet received on a channel and modifies the server state properly.
   *
   * @param state   the old state of the server.
   * @param packet  the packet to be processed.
   * @param channel the channel on which the packet has been received
   * @return the new state of the server.
   */
  def handle(state: State, packet: Packet, channel: Channel): State
  
  /**
   * Checks whether there are active clients whose timespan from last contact doesn't respect the keep alive specified during connection.
   * In that case, the client is forcibly disconnected.
   *
   * @param state the old state of the server.
   * @return the new state of the server.
   */
  def tick(state: State): State
}
