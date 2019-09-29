package mqtt.broker.handlers

import mqtt.broker.Common.closeChannelNoWillPublish
import mqtt.broker.state.{Channel, State}
import mqtt.model.Packet.Disconnect

/**
 * Represents and handler for disconnect packets.
 *
 * @param packet  the disconnect packet to handle.
 * @param channel the channel on which the packet has been received.
 */
case class DisconnectPacketHandler(override val packet: Disconnect, override val channel: Channel) extends PacketHandler[Disconnect] {
  override def handle: State => State = state => {
    closeChannelNoWillPublish(channel, Seq())(state)
  }
}
