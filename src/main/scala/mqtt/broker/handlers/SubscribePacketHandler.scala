package mqtt.broker.handlers

import mqtt.broker.Common
import mqtt.broker.Common.updateLastContact
import mqtt.broker.handlers.SubscribePacketHandler.maxAllowedQoS
import mqtt.broker.state.StateImplicits._
import mqtt.broker.state.Violation.{GenericViolation, SubscriptionTopicListEmpty}
import mqtt.broker.state.{Channel, State, Violation}
import mqtt.model.Packet.{ApplicationMessage, Suback, Subscribe}
import mqtt.model.Types.ClientID
import mqtt.model.{QoS, TopicFilter}

case class SubscribePacketHandler(override val packet: Subscribe, override val channel: Channel) extends PacketHandler[Subscribe] with AutoViolationHandler {
  type FilterOptions = Seq[Option[(TopicFilter, QoS)]]
  
  override def handle: State => State = {
    for {
      clientID <- getClientID
      _ <- checkAtLeastOneSubscription
      filterOptions <- validateFilters
      _ <- storeSubscriptions(filterOptions)
      _ <- sendSUBACK(clientID, filterOptions)
      _ <- publishRetains(clientID, filterOptions)
      _ <- updateLastContact(channel)
    } yield ()
  }
  
  def getClientID: State => Either[Violation, (ClientID, State)] = state => {
    state.sessionFromChannel(channel)
      .fold[Either[Violation, (ClientID, State)]] {
        Left(new GenericViolation("Subscribe: unexpected error, client id not found."))
      } { case (id, _) => Right(id, state) }
  }
  
  def checkAtLeastOneSubscription: State => Either[Violation, State] = state => {
    if (packet.topics.isEmpty) Left(SubscriptionTopicListEmpty()) else Right(state)
  }
  
  def validateFilters: State => (FilterOptions, State) = state => {
    (packet.topics.map { case (str, qos) => TopicFilter(str).map(f => (f, PublishPacketHandler.min(maxAllowedQoS, qos))) }, state)
  }
  
  def storeSubscriptions(filterOptions: FilterOptions): State => State = {
    filterOptions.collect { case Some(fq) => fq }.map { case (filter, qos) => storeSubscription(filter, qos) }
      .foldLeft[State => State](s => s)(_ andThen _)
  }
  
  def storeSubscription(filter: TopicFilter, qos: QoS): State => State = state => {
    state.updateSessionFromChannel(channel, session => {
      val newSubs = session.subscriptions + ((filter, qos))
      session.copy(subscriptions = newSubs)
    })
  }
  
  def getRetainsFromFilter(filter: TopicFilter): State => Seq[ApplicationMessage] = state => {
    state.retains.collect { case (topic, msg) if filter.matching(topic) => msg }.toSeq
  }
  
  def getRetains(filterOptions: FilterOptions): State => Seq[(ApplicationMessage, QoS)] = state => {
    filterOptions.collect { case Some(t) => t }.flatMap { case (filter, qos) => getRetainsFromFilter(filter)(state).map(f => (f, qos)) }
  }
  
  def publishRetain(clientID: ClientID, msg: ApplicationMessage, qos: QoS): State => State = state => {
    val msgToSend = ApplicationMessage(retain = true, PublishPacketHandler.min(qos, msg.qos), msg.topic, msg.payload)
    PublishPacketHandler.publishMessageTo(clientID, msgToSend)(state)
  }
  
  def publishRetains(clientID: ClientID, filterOptions: FilterOptions): State => State = state => {
    getRetains(filterOptions)(state).map { case (msg, qos) => publishRetain(clientID, msg, qos) }.foldLeft[State => State](s => s)(_ andThen _)(state)
  }
  
  def sendSUBACK(clientID: ClientID, filterOptions: FilterOptions): State => State = state => {
    val qosses = filterOptions.map(opt => opt.map { case (_, qos) => qos })
    val ack = Suback(packet.packetId, qosses)
    Common.sendPacket(clientID, ack)(state)
  }
}

object SubscribePacketHandler {
  val maxAllowedQoS = QoS(0)
}