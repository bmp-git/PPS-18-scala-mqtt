package mqtt.broker.handlers

import mqtt.broker.Common.closeChannelNoWillPublish
import mqtt.broker.state.{Channel, State}
import mqtt.model.Packet.Disconnect

/**
 * Handles disconnect packets.
 */
case class DisconnectPacketHandler(override val packet: Disconnect, override val channel: Channel) extends PacketHandler[Disconnect] {
  override def handle: State => State = state => {
    closeChannelNoWillPublish(channel, Seq())(state)
  }
}
