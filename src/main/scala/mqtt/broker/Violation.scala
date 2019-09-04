package mqtt.broker

import mqtt.Socket
import mqtt.model.Packet
import mqtt.model.Packet.Connack
import mqtt.model.Packet.ConnectReturnCode._

trait Violation {
  def msg: String
  def handle(socket: Socket)(state: State): State = {
    state.addClosingChannel(socket, closePackets())
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
