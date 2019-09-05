package mqtt.broker

import mqtt.model.Packet
import mqtt.model.Packet.Connack
import mqtt.model.Packet.ConnectReturnCode._
import mqtt.broker.StateImplicits.StateTransitionWithError_Implicit


import mqtt.broker.Common.closeSocketWithPackets

trait Violation {
  def msg: String
  def handle(socket: Socket): State => State = state =>  {
    closeSocketWithPackets(socket, closePackets())(state)
  }
  def closePackets(): Seq[Packet]
}

object Violation {
  case class GenericViolation(msg: String) extends Violation {
    override val closePackets: Seq[Connack] = Seq()
  }
  
  case class InvalidProtocolVersion() extends Violation {
    override val msg: String = "InvalidProtocolVersion"
    override val closePackets: Seq[Connack] = Seq(Connack(sessionPresent = false, UnacceptableProtocolVersion))
  }
  
  case class InvalidIdentifier() extends Violation {
    override val msg: String = "InvalidIdentifier"
    override val closePackets: Seq[Connack] = Seq(Connack(sessionPresent = false, IdentifierRejected))
  }
}
