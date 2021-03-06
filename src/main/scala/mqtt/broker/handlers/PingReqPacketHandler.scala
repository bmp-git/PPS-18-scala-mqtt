package mqtt.broker.handlers

import mqtt.broker.Common
import mqtt.broker.Common.{assertClientConnected, updateLastContact}
import mqtt.broker.state.StateImplicits._
import mqtt.broker.state.{Channel, State}
import mqtt.model.Packet.{Pingreq, Pingresp}

/**
 * Represents an handler for PINGREQ packets.
 *
 * @param packet  the PINGREQ packet to handle.
 * @param channel the channel on which the packet has been received.
 */
case class PingReqPacketHandler(override val packet: Pingreq, override val channel: Channel)
  extends PacketHandler[Pingreq] with AutoViolationHandler {
  
  override def handle: State => State = {
    for {
      _ <- assertClientConnected(channel)
      _ <- sendPINGRESP
      _ <- updateLastContact(channel)
    } yield ()
  }
  
  /**
   * Sends a PINGRESP to the client that sent the PINGREQ.
   *
   * @return a function that maps the old server state in the new one.
   */
  def sendPINGRESP: State => State = Common.sendPacketOnChannel(channel, Pingresp())
}
