package mqtt.broker.handlers

import mqtt.broker.Common.sendPacket
import mqtt.broker.handlers.PublishPacketHandler._
import mqtt.broker.state.StateImplicits._
import mqtt.broker.state.Violation.{InvalidQoSDupPair, InvalidTopicName}
import mqtt.broker.state.{Channel, State, Violation}
import mqtt.model.Packet.{ApplicationMessage, Publish}
import mqtt.model.Types.{ClientID, Payload}
import mqtt.model.{QoS, Topic}


case class PublishPacketHandler(override val packet: Publish, override val channel: Channel) extends PacketHandler[Publish] with AutoViolationHandler {
  override def handle: State => State = {
    for {
      _ <- checkValidQoSDupPair
      topic <- checkValidTopic
      _ <- handleRetain(topic, packet.message)
      _ <- publishMessage(packet.message.qos, packet.message.topic, packet.message.payload)
    } yield ()
  }
  
  def checkValidQoSDupPair: State => Either[Violation, State] = state => {
    val valid = packet.message.qos match {
      case QoS(0) => !packet.duplicate
      case _ => true
    }
    if (valid) Right(state) else Left(InvalidQoSDupPair())
  }
  
  def checkValidTopic: State => Either[Violation, (Topic, State)] = state => {
    Topic(packet.message.topic).fold[Either[Violation, (Topic, State)]](Left(InvalidTopicName()))(t => Right((t, state)))
  }
  
  
}

object PublishPacketHandler {
  def handleRetain(topic: Topic, message: ApplicationMessage): State => State = state => {
    if (message.retain) retainMessage(topic, message)(state) else state
  }
  
  def retainMessage(topic: Topic, message: ApplicationMessage): State => State = state => {
    if (message.payload.isEmpty) state.clearRetainMessage(topic) else state.setRetainMessage(topic, message)
  }
  
  def min(firstQoS: QoS, secondQoS: QoS): QoS = (firstQoS, secondQoS) match {
    case (QoS(first), QoS(second)) => if (first < second) firstQoS else secondQoS
  }
  
  def getMatchingSubscriptionsAndMaxQoS(topic: Topic, qoS: QoS): State => Map[ClientID, Iterable[QoS]] = state => {
    for {
      (clientId, session) <- state.sessions
      if session.channel.isDefined //check the session is active TODO for QoS1 and 2 msg are saved anyway?
      qosses = for {
        (filter, qos) <- session.subscriptions
        if filter.matching(topic)
        maxQos = min(qos, qoS)
      } yield (maxQos)
    } yield ((clientId, qosses))
  }
  
  def publishMessageTo(clientID: ClientID, message: ApplicationMessage): State => State = state => message.qos match {
    case QoS(0) => publishMessageToWithQoS0(clientID, message)(state)
    case QoS(1) => ???
    case QoS(2) => ???
  }
  
  def publishMessageToWithQoS0(clientID: ClientID, message: ApplicationMessage): State => State = state => {
    val packet = Publish(duplicate = false, packetId = 0, message = message)
    sendPacket(clientID, packet)(state)
  }
  
  /**
   * Publishes a payload to all clients with a subscription matching the topic with a maximum QoS (could be degraded on a per client basis).
   *
   * @param qos     the maximum quality of service.
   * @param topic   the topic where to publish.
   * @param payload the payload to publish.
   * @return a function that maps the old server state in the new one.
   */
  def publishMessage(qos: QoS, topic: String, payload: Payload): State => State = state => {
    val msgToSend = ApplicationMessage(retain = false, qos, topic, payload)
    
    Topic(topic).fold(state)(topic => {
      val subs = getMatchingSubscriptionsAndMaxQoS(topic, qos)(state)
      
      val publishFunctions = for {
        (clientID, qosses) <- subs
        qos <- qosses
        msg = msgToSend.copy(qos = qos)
        fun = publishMessageTo(clientID, msg)
      } yield fun
      
      publishFunctions.foldLeft[State => State](s => s)(_ andThen _)(state)
      
    })
  }
}
