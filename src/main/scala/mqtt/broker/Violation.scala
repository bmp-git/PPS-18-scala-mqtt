package mqtt.broker

import mqtt.broker.Common.closeSocketWithPackets
import mqtt.model.Packet
import mqtt.model.Packet.Connack
import mqtt.model.Packet.ConnectReturnCode._

/**
 * Represents a violation of the protocol. After a violation the client must be forcibly disconnected.
 */
trait Violation {
  /**
   * The message for logging purposes.
   *
   * @return the message.
   */
  def msg: String
  
  /**
   * Handles the violation, disconnecting the client related to the socket specified.
   * Optionally, before closing the socket, a sequence of packets will be sent.
   *
   * @param socket the socket on which the violation has happened.
   * @return a function that maps the old server state in the new one.
   */
  def handle(socket: Socket): State => State = state =>  {
    closeSocketWithPackets(socket, closePackets())(state)
  }
  
  /**
   * Gets the sequence of packets to be sent before closing.
   *
   * @return the packets sequence.
   */
  def closePackets(): Seq[Packet]
  
  override def toString: String = msg
}

/**
 * Contains common violations.
 */
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
