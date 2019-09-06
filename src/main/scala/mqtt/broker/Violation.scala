package mqtt.broker

import mqtt.model.Packet
import mqtt.model.Packet.Connack
import mqtt.model.Packet.ConnectReturnCode._


import mqtt.broker.Common.closeSocketWithPackets

trait Violation {
  def msg: String
  def handle(socket: Socket): State => State = state =>  {
    closeSocketWithPackets(socket, closePackets())(state)
  }
  def closePackets(): Seq[Packet]
  override def toString: String = msg
}

object Violation {
  class GenericViolation(override val msg: String) extends Violation {
    override val closePackets: Seq[Connack] = Seq()
  }
  
  case class InvalidProtocolName() extends GenericViolation("InvalidProtocolName")
  
  case class InvalidProtocolVersion() extends GenericViolation("InvalidProtocolVersion") {
    override val closePackets: Seq[Connack] = Seq(Connack(sessionPresent = false, UnacceptableProtocolVersion))
  }
  
  case class InvalidIdentifier() extends GenericViolation("InvalidIdentifier") {
    override val closePackets: Seq[Connack] = Seq(Connack(sessionPresent = false, IdentifierRejected))
  }
  
  case class MultipleConnectPacketsOnSameSocket() extends GenericViolation("MultipleConnectPacketsOnSameSocket")
}
