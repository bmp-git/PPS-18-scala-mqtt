package mqtt.broker.handlers

import mqtt.broker.Common.{assertClientConnected, closeChannelNoWillPublish}
import mqtt.broker.state.StateImplicits._
import mqtt.broker.state.{Channel, State}
import mqtt.model.Packet.Disconnect

/**
 * Represents an handler for disconnect packets.
 *
 * @param packet  the disconnect packet to handle.
 * @param channel the channel on which the packet has been received.
 */
case class DisconnectPacketHandler(override val packet: Disconnect, override val channel: Channel) extends PacketHandler[Disconnect] with AutoViolationHandler {
  override def handle: State => State = {
    for {
      _ <- assertClientConnected(channel)
      _ <- closeChannelNoWillPublish(channel, Seq())
    } yield ()
  }
}
