package mqtt.broker.handlers

import mqtt.broker.Common
import mqtt.broker.Common.{assertClientConnected, sendPacketToClient, updateLastContact}
import mqtt.broker.handlers.PublishPacketHandler._
import mqtt.broker.state.StateImplicits._
import mqtt.broker.state.Violation.{InvalidQoSDupPair, InvalidTopicName, qoSNotSupported}
import mqtt.broker.state.{Channel, State, Violation}
import mqtt.model.Packet.{ApplicationMessage, Publish}
import mqtt.model.Types.{ClientID, Payload}
import mqtt.model.{QoS, Topic}


/**
 * Represents an handler for publish packets.
 *
 * @param packet  the publish packet to handle.
 * @param channel the channel on which the packet has been received.
 */
case class PublishPacketHandler(override val packet: Publish, override val channel: Channel) extends PacketHandler[Publish] with AutoViolationHandler {
  override def handle: State => State = {
    for {
      _ <- assertClientConnected(channel)
      _ <- checkSupportedQoS
      _ <- checkValidQoSDupPair
      topic <- checkValidTopic
      _ <- handleRetain(topic, packet.message)
      _ <- publishMessage(packet.message.qos, topic, packet.message.payload)
      _ <- updateLastContact(channel)
    } yield ()
  }
  
  /**
   * Checks whether the specified QoS is supported by the current implementation.
   *
   * @return a function that maps a state to a violation or to a new state.
   */
  def checkSupportedQoS: State => Either[Violation, State] = state => {
    //TODO currently only supporting QoS0
    val valid = packet.message.qos match {
      case QoS(0) => true
      case _ => false
    }
    if (valid) Right(state) else Left(qoSNotSupported)
  }
  
  /**
   * Checks whether the specified QoS Duplicate flags are compatible (ex. if QoS0 then DUP must be false).
   *
   * @return a function that maps a state to a violation or to a new state.
   */
  def checkValidQoSDupPair: State => Either[Violation, State] = state => {
    val valid = packet.message.qos match {
      case QoS(0) => !packet.duplicate
      case _ => true
    }
    if (valid) Right(state) else Left(InvalidQoSDupPair)
  }
  
  /**
   * Checks whether the specified topic on which to publish is valid.
   *
   * @return a function that maps a state to a violation or to a tuple with the topic instance and the new state.
   */
  def checkValidTopic: State => Either[Violation, (Topic, State)] = state => {
    Topic(packet.message.topic).fold[Either[Violation, (Topic, State)]](Left(InvalidTopicName))(t => Right((t, state)))
  }
  

  
}

/**
 * Contains functions relative to publish actions that can be used by other handlers.
 */
object PublishPacketHandler {
  /**
   * Checks whether the message should be retained. In that case the message is store in state.
   *
   * @param topic   the topic relative to the retain message.
   * @param message the message to optionally retain.
   * @return a function that maps the old server state in the new one.
   */
  def handleRetain(topic: Topic, message: ApplicationMessage): State => State = state => {
    if (message.retain) retainMessage(topic, message)(state) else state
  }
  
  /**
   * Retains a message, only if the payload is not empty. In case the payload is empty, the retain message will be cleared for that topic.
   *
   * @param topic   the topic relative to the retain message.
   * @param message the message to retain.
   * @return a function that maps the old server state in the new one.
   */
  def retainMessage(topic: Topic, message: ApplicationMessage): State => State = state => {
    if (message.payload.isEmpty) state.clearRetainMessage(topic) else state.setRetainMessage(topic, message)
  }
  
  /**
   * Gets all the client identifiers with a subscription that matches the specified topic. A client could have multiple matching subscriptions to that topic.
   * For each matching subscription, the maximum QoS allowed for that subscription is computed as min(specified maximum QoS qos, qos of the subscription).
   *
   * @param topic the topic to match.
   * @param qoS   the maximum QoS allowed.
   * @return a Map containing, for each client ID with at least a matching subscription, the maximum QoSs allowed for each matching subscription.
   */
  def getMatchingSubscriptionsAndMaxQoS(topic: Topic, qoS: QoS): State => Map[ClientID, Iterable[QoS]] = state => {
    for {
      (clientId, session) <- state.sessions
      if session.channel.isDefined //check the session is active TODO for QoS1 and 2 msg are saved anyway?
      qosses = for {
        (filter, qos) <- session.subscriptions
        if filter.matching(topic)
        maxQos = Common.min(qos, qoS)
      } yield (maxQos)
    } yield ((clientId, qosses))
  }
  
  /**
   * Publishes an application message to a specified ClientID (will send a Publish message to him). The QoS is specified by the application message.
   *
   * @param clientID the recipient client id.
   * @param message  the application message to send (specifies the QoS).
   * @return a function that maps the old server state in the new one.
   */
  def publishMessageTo(clientID: ClientID, message: ApplicationMessage): State => State = state => message.qos match {
    case QoS(0) => publishMessageToWithQoS0(clientID, message)(state)
    case QoS(1) => state //TODO
    case QoS(2) => state //TODO
  }
  
  /**
   * Publishes an application message to a specified ClientID with QoS0.
   *
   * @param clientID the recipient client id.
   * @param message  the application message to send.
   * @return a function that maps the old server state in the new one.
   */
  def publishMessageToWithQoS0(clientID: ClientID, message: ApplicationMessage): State => State = state => {
    val packet = Publish(duplicate = false, packetId = 0, message = message)
    sendPacketToClient(clientID, packet)(state)
  }
  
  /**
   * Publishes a payload to all clients with a subscription matching the topic with a maximum QoS (could be degraded on a per client basis).
   *
   * @param qos     the maximum quality of service.
   * @param topic   the topic where to publish.
   * @param payload the payload to publish.
   * @return a function that maps the old server state in the new one.
   */
  def publishMessage(qos: QoS, topic: Topic, payload: Payload): State => State = state => {
    val msgToSend = ApplicationMessage(retain = false, qos, topic.value, payload)
    val subs = getMatchingSubscriptionsAndMaxQoS(topic, qos)(state)
  
    val publishFunctions = for {
      (clientID, qosses) <- subs
      qos <- qosses
      msg = msgToSend.copy(qos = qos)
      fun = publishMessageTo(clientID, msg)
    } yield fun
  
    publishFunctions.foldLeft[State => State](s => s)(_ andThen _)(state)
  }
}
