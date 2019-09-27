package mqtt.broker.state

import mqtt.broker.Common.closeChannelWithPackets
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
   * Handles the violation, disconnecting the client related to the channel specified.
   * Optionally, before closing the channel, a sequence of packets will be sent.
   *
   * @param channel the channel on which the violation has happened.
   * @return a function that maps the old server state in the new one.
   */
  def handle(channel: Channel): State => State = state => {
    closeChannelWithPackets(channel, closePackets())(state)
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
  
  case class MultipleConnectPacketsOnSameChannel() extends GenericViolation("MultipleConnectPacketsOnSameChannel")
  
  case class InvalidTopicName() extends GenericViolation("InvalidTopicName")
  
  case class InvalidQoSDupPair() extends GenericViolation("InvalidQoSDupPair")
}
