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
  
  case class InvalidProtocolVersion() extends Violation {
    override def msg: String = "InvalidProtocolVersion"
    override val closePackets: Seq[Connack] = Seq(Connack(sessionPresent = false, UnacceptableProtocolVersion))
  }
  
  case class InvalidIdentifier() extends Violation {
    override def msg: String = "InvalidIdentifier"
    override val closePackets: Seq[Connack] = Seq(Connack(sessionPresent = false, IdentifierRejected))
  }
  
  case class ClientNotAuthorized() extends Violation {
    override def msg: String = "ClientNotAuthorized"
    override val closePackets: Seq[Connack] = Seq(Connack(sessionPresent = false, NotAuthorized))
  }
  
  case class MultipleConnectPacketsOnSameChannel() extends GenericViolation("MultipleConnectPacketsOnSameChannel")
  
  case class ClientIsNotConnected() extends GenericViolation("ClientIsNotConnected")
  
  case class InvalidTopicName() extends GenericViolation("InvalidTopicName")
  
  case class qoSNotSupported() extends GenericViolation("qoSNotSupported")
  
  case class InvalidQoSDupPair() extends GenericViolation("InvalidQoSDupPair")
  
  case class SubscriptionTopicListEmpty() extends GenericViolation("SubscriptionTopicListEmpty")
  
  case class UnsubscriptionTopicListEmpty() extends GenericViolation("UnsubscriptionTopicListEmpty")
  
  case class InvalidWillTopic() extends GenericViolation("InvalidWillTopic")
}
