package mqtt.broker

import mqtt.model.Packet.ApplicationMessage

//TODO this is a draft to be refined

/**
 * Represents a communication channel related to a client.
 *
 * @param id          the id to identify the socket
 * @param willMessage the willMessage associated with this communication channel.
 */
case class Socket(id: Int, willMessage: Option[ApplicationMessage]) extends Channel[Seq, Byte] {
  override def send(m: Seq[Byte]): Unit = ()
  
  def setWillMessage(willMessage: Option[ApplicationMessage]): Socket = {
    this.copy(willMessage = willMessage)
  }
}
