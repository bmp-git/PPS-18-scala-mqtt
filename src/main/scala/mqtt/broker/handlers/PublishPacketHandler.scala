package mqtt.broker.handlers

import mqtt.broker.state.{Channel, State}
import mqtt.model.Packet.Publish

object PublishPacketHandler extends PacketHandler[Publish] {
  /**
   * Handles a publish packet received on a channel and modifies the server state properly.
   *
   * @param state   the old state of the server.
   * @param packet  the publish packet to be processed.
   * @param channel the channel on which the packet has been received
   * @return the new state of the server.
   */
  override def handle(state: State, packet: Publish, channel: Channel): State = {
    ???
  }
}
