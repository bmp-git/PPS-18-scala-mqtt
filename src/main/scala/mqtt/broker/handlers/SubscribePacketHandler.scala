package mqtt.broker.handlers

import mqtt.broker.Common
import mqtt.broker.Common.{assertClientConnected, updateLastContact}
import mqtt.broker.handlers.SubscribePacketHandler.maxAllowedQoS
import mqtt.broker.state.StateImplicits._
import mqtt.broker.state.Violation.SubscriptionTopicListEmpty
import mqtt.broker.state.{Channel, State, Violation}
import mqtt.model.Packet.{ApplicationMessage, Suback, Subscribe}
import mqtt.model.Types.ClientID
import mqtt.model.{QoS, TopicFilter}

/**
 * Represents an handler for subscribe packets.
 *
 * @param packet  the subscribe packet to handle.
 * @param channel the channel on which the packet has been received.
 */
case class SubscribePacketHandler(override val packet: Subscribe, override val channel: Channel) extends PacketHandler[Subscribe] with AutoViolationHandler {
  /**
   * Represents a sequence of TopicFilter with its QoS. If the option is empty, the relative topic filter string was not valid.
   */
  type FilterOptions = Seq[Option[(TopicFilter, QoS)]]
  
  override def handle: State => State = {
    for {
      clientID <- assertClientConnected(channel)
      _ <- checkAtLeastOneSubscription
      filterOptions <- validateFilters
      _ <- storeSubscriptions(filterOptions)
      _ <- sendSUBACK(filterOptions)
      _ <- publishRetains(clientID, filterOptions)
      _ <- updateLastContact(channel)
    } yield ()
  }
  
  /**
   * Checks whether the subscription list of the packet is not empty.
   *
   * @return a function that maps a state to a violation or to a new state.
   */
  def checkAtLeastOneSubscription: State => Either[Violation, State] = state => {
    if (packet.topics.isEmpty) Left(SubscriptionTopicListEmpty) else Right(state)
  }
  
  /**
   * Checks whether the filters of the subscriptions of the packet are valid. Returns an option empty for those who are not.
   * The QoS specified in the returned FilterOptions is computed as min(maxAllowedQoS by the server, qos requested)
   *
   * @return a function that maps a state to a tuple containing the new state and the FilterOptions sequence.
   */
  def validateFilters: State => (FilterOptions, State) = state => {
    (packet.topics.map { case (str, qos) => TopicFilter(str).map(f => (f, Common.min(maxAllowedQoS, qos))) }, state)
  }
  
  /**
   * Stores all the valid subscriptions into the State.
   *
   * @param filterOptions the sequence containing the subscriptions.
   * @return a function that maps the old server state in the new one.
   */
  def storeSubscriptions(filterOptions: FilterOptions): State => State = {
    filterOptions.collect { case Some(fq) => fq }.map { case (filter, qos) => storeSubscription(filter, qos) }
      .foldLeft[State => State](identity)(_ andThen _)
  }
  
  /**
   * Stores a subscription with its QoS into the State.
   *
   * @param filter the topic filter relative to the subscription.
   * @param qos    the maximum QoS associated to the subscription.
   * @return a function that maps the old server state in the new one.
   */
  def storeSubscription(filter: TopicFilter, qos: QoS): State => State = state => {
    state.updateSessionFromChannel(channel, session => {
      val newSubs = session.subscriptions + ((filter, qos))
      session.copy(subscriptions = newSubs)
    })
  }
  
  /**
   * Gets all the retain messages from the topics that matches the specified filter.
   *
   * @param filter the filter to match.
   * @return a function that maps the server state into a sequence of application message.
   */
  def getRetainsFromFilter(filter: TopicFilter): State => Seq[ApplicationMessage] = state => {
    state.retains.collect { case (topic, msg) if filter.matching(topic) => msg }.toSeq
  }
  
  /**
   * Gets all the retain messages from the topics that matches the specified sequence of filters.
   *
   * @param filterOptions the sequence of filters to match.
   * @return a function that maps the server state into a sequence of application message, each one with the QoS specified in the FilterOptions.
   */
  def getRetains(filterOptions: FilterOptions): State => Seq[(ApplicationMessage, QoS)] = state => {
    filterOptions.collect { case Some(t) => t }.flatMap { case (filter, qos) => getRetainsFromFilter(filter)(state).map(f => (f, qos)) }
  }
  
  /**
   * Publishes an ApplicationMessage as a retain message to a specified client with a specified maximum QoS.
   *
   * @param clientID the recipient of the publish message.
   * @param msg      the ApplicationMessage to send as retain.
   * @param qos      maximum QoS to limit the QoS specified in the retain message.
   * @return
   */
  def publishRetain(clientID: ClientID, msg: ApplicationMessage, qos: QoS): State => State = state => {
    val msgToSend = ApplicationMessage(retain = true, Common.min(qos, msg.qos), msg.topic, msg.payload)
    PublishPacketHandler.publishMessageTo(clientID, msgToSend)(state)
  }
  
  /**
   * Publishes all the retain messages matched by the FilterOptions to a specified client.
   *
   * @param clientID      the recipient of the publish messages.
   * @param filterOptions the list of filters to match the retain topics.
   * @return a function that maps the old server state in the new one.
   */
  def publishRetains(clientID: ClientID, filterOptions: FilterOptions): State => State = state => {
    getRetains(filterOptions)(state).map { case (msg, qos) => publishRetain(clientID, msg, qos) }.foldLeft[State => State](identity)(_ andThen _)(state)
  }
  
  /**
   * Sends an SUBACK to the client that requested the subscription.
   * The return codes are derived from the filterOptions specified.
   *
   * @param filterOptions the list of filters with the QoS granted by the server.
   * @return a function that maps the old server state in the new one.
   */
  def sendSUBACK(filterOptions: FilterOptions): State => State = state => {
    val qosses = filterOptions.map(opt => opt.map { case (_, qos) => qos })
    val ack = Suback(packet.packetId, qosses)
    Common.sendPacketOnChannel(channel, ack)(state)
  }
}

object SubscribePacketHandler {
  /**
   * Maximum allowed QoS by the current implementation.
   */
  val maxAllowedQoS = QoS(0)
}