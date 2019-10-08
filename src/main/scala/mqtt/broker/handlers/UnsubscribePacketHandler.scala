package mqtt.broker.handlers

import mqtt.broker.Common
import mqtt.broker.Common.{assertClientConnected, updateLastContact}
import mqtt.broker.state.StateImplicits._
import mqtt.broker.state.Violation.UnsubscriptionTopicListEmpty
import mqtt.broker.state.{Channel, State, Violation}
import mqtt.model.Packet.{Unsuback, Unsubscribe}
import mqtt.model.TopicFilter

/**
 * Represents an handler for unsubscribe packets.
 *
 * @param packet  the unsubscribe packet to handle.
 * @param channel the channel on which the packet has been received.
 */
case class UnsubscribePacketHandler(override val packet: Unsubscribe, override val channel: Channel)
  extends PacketHandler[Unsubscribe] with AutoViolationHandler {
  
  override def handle: State => State = {
    for {
      _ <- assertClientConnected(channel)
      _ <- checkAtLeastOneUnsubscription
      _ <- eraseSubscriptions
      _ <- sendUNSUBACK
      _ <- updateLastContact(channel)
    } yield ()
  }
  
  /**
   * Checks whether the unsubscription list of the packet is not empty.
   *
   * @return a function that maps a state to a violation or to a new state.
   */
  def checkAtLeastOneUnsubscription: State => Either[Violation, State] = state => {
    if (packet.topics.isEmpty) Left(UnsubscriptionTopicListEmpty) else Right(state)
  }
  
  /**
   * Removes all the subscriptions (topic filters) of the client that exactly match the filters specified in the unsubscribe packet.
   *
   * @return a function that maps the old server state in the new one.
   */
  def eraseSubscriptions: State => State = state => {
    val filters = packet.topics.map(TopicFilter.apply).collect { case Some(f) => f }
    state.updateSessionFromChannel(channel, sess => {
      val newSubs = sess.subscriptions -- filters
      sess.copy(subscriptions = newSubs)
    })
  }
  
  /**
   * Sends an UNSUBACK to the client that requested the unsubscription.
   *
   * @return a function that maps the old server state in the new one.
   */
  def sendUNSUBACK: State => State = Common.sendPacketOnChannel(channel, Unsuback(packet.packetId))
  
}
