package mqtt.broker.handlers

import mqtt.broker.state.{Channel, State}
import mqtt.model.Packet

/**
 * Represents the logic to modify the server state at the moment of a reception of a particular kind of packet.
 *
 * @tparam T the kind of the packet.
 */
trait PacketHandler[T <: Packet] {
  /**
   * The packet of kind T to be processed.
   *
   * @return the packet.
   */
  def packet: T
  
  /**
   * The channel on which the packet has been received.
   *
   * @return the channel.
   */
  def channel: Channel
  /**
   * Handles a packet of kind T received on a channel and modifies the server state properly.
   *
   * @return a function that maps the old state of the server in a new one.
   */
  def handle: State => State
}
