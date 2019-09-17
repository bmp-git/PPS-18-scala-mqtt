package mqtt.broker

import mqtt.broker.Common.closeChannelNoWillPublish
import mqtt.model.Packet.Disconnect

/**
 * Handles disconnect packets.
 */
object DisconnectPacketHandler extends PacketHandler[Disconnect] {
  override def handle(state: State, packet: Disconnect, channel: Channel): State = {
    closeChannelNoWillPublish(channel, Seq())(state)
  }
}
