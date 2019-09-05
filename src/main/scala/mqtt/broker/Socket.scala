package mqtt.broker

import mqtt.model.Packet.ApplicationMessage

case class Socket(id: Int, willMessage: Option[ApplicationMessage]) extends Channel[Seq, Byte] {
  override def send(m: Seq[Byte]): Unit = ()
  def setWillMessage(willMessage: Option[ApplicationMessage]): Socket = {
    this.copy(willMessage = willMessage)
  }
}
