package mqtt.broker.handlers

import mqtt.broker.Common.closeChannelNoWillPublish
import mqtt.broker.state.{Channel, State}
import mqtt.model.Packet.Disconnect

/**
 * Handles disconnect packets.
 */
object DisconnectPacketHandler extends PacketHandler[Disconnect] {
  override def handle(state: State, packet: Disconnect, channel: Channel): State = {
    closeChannelNoWillPublish(channel, Seq())(state)
  }
}
