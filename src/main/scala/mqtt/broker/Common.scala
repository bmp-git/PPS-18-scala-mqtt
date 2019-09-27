package mqtt.broker

import mqtt.broker.state.{Channel, State}
import mqtt.model.Packet.{ApplicationMessage, Publish}
import mqtt.model.Types.ClientID
import mqtt.model.{Packet, QoS, Topic}

/**
 * Contains common utility methods to modify the state of the server.
 */
object Common {
  /**
   * Closes a channel, publishes the will message if present and updates or deletes the client session.
   *
   * @param channel the channel to be closed.
   * @return a function that maps the old server state in the new one.
   */
  def closeChannel(channel: Channel): State => State = state => {
    closeChannelWithPackets(channel, Seq())(state)
  }
  
  /**
   * Closes a channel, publishes the will message if present and updates or deletes the client session.
   *
   * @param channel      the channel to be closed.
   * @param closePackets the packets to be sent before closing.
   * @return a function that maps the old server state in the new one.
   */
  def closeChannelWithPackets(channel: Channel, closePackets: Seq[Packet]): State => State = state => {
    (publishWillMessage(channel) andThen closeChannelNoWillPublish(channel, closePackets)) (state)
  }
  
  /**
   * Closes a channel and updates or deletes the client session.
   * Removes the will message if present.
   *
   * @param channel      the channel to be closed.
   * @param closePackets the packets to be sent before closing.
   * @return a function that maps the old server state in the new one.
   */
  def closeChannelNoWillPublish(channel: Channel, closePackets: Seq[Packet]): State => State = state => {
    (updateSessionAfterChannelDisconnection(channel) andThen deleteWillMessage(channel)) (state).addClosingChannel(channel, closePackets)
  }
  
  /**
   * Updates or deletes the client session relative to the channel, if present.
   * If the session is persistent, the session is updated removing the channel.
   * If the session is not persistent, the session is removed from the server state.
   *
   * @return a function that maps the old server state in the new one.
   */
  def updateSessionAfterChannelDisconnection(channel: Channel): State => State = state => {
    state.sessionFromChannel(channel).fold(state) { case (id, sess) => {
      if (sess.persistent) {
        val newSess = sess.copy(channel = Option.empty)
        state.updateSession(id, _ => newSess)
      } else {
        state.deleteSession(id)
      }
    }
    }
  }
  
  /**
   * Deletes the will message related to a channel.
   *
   * @param channel the channel.
   * @return a function that maps the old server state in the new one.
   */
  def deleteWillMessage(channel: Channel): State => State = _.deleteWillMessage(channel)
  
  /**
   * Publishes the will message related to a channel if present.
   *
   * @param channel the channel
   * @return a function that maps the old server state in the new one.
   */
  def publishWillMessage(channel: Channel): State => State = state => {
    state.wills.get(channel).fold[State](state)(publishMessage(_)(state))
  }
  
  def sendPacket(clientID: ClientID, packet: Packet): State => State = state => {
    state.updateSession(clientID, s => {
      val newPending = s.pendingTransmission ++ Seq(packet)
      s.copy(pendingTransmission = newPending)
    })
  }
  
  def min(firstQoS: QoS, secondQoS: QoS): QoS = (firstQoS, secondQoS) match {
    case (QoS(first), QoS(second)) => if (first < second) firstQoS else secondQoS
  }
  
  def getMatchingSubscriptionsAndMaxQoS(topic: Topic, qoS: QoS): State => Map[ClientID, Iterable[QoS]] = state => {
    for {
      (clientId, session) <- state.sessions
      if session.channel.isDefined //check the session is active
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
   * Publishes a message.
   *
   * @param message the message to publish.
   * @return a function that maps the old server state in the new one.
   */
  def publishMessage(message: ApplicationMessage): State => State = state => {
    //TODO
    println("Message published ".concat(message.toString))
    
    Topic(message.topic).fold(state)(topic => {
      val subs = getMatchingSubscriptionsAndMaxQoS(topic, message.qos)(state)
      
      val publishfunctions = for {
        (clientID, qosses) <- subs
        qos <- qosses
        msg = message.copy(retain = false, qos = qos)
        fun = publishMessageTo(clientID, msg)
      } yield (fun)
    
      publishfunctions.foldLeft[State => State](s => s)(_ andThen _)(state)
    
    })
  }
  
}
