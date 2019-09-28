package mqtt.broker.state

import mqtt.model.Packet.ApplicationMessage
import mqtt.model.Types.ClientID
import mqtt.model.{Packet, Topic}

/**
 * Represents the internal state of the server/broker.
 *
 * @param sessions the client sessions to be initialized with.
 * @param retains  the retain messages to be initialized with.
 * @param closing  the closing channels to be initialized with.
 * @param wills    the will messages to be initialized with.
 */
case class BrokerState(override val sessions: Map[ClientID, Session],
                       override val retains: Map[Topic, Packet.ApplicationMessage],
                       override val closing: Map[Channel, Seq[Packet]],
                       override val wills: Map[Channel, ApplicationMessage]) extends State {
  override def sessionFromClientID(clientID: ClientID): Option[Session] = sessions.get(clientID)
  
  override def sessionFromChannel(channel: Channel): Option[(ClientID, Session)] = {
    sessions.collectFirst { case (id, s) if s.channel.fold(false)(_ == channel) => (id, s) }
  }
  
  override def setSession(clientID: ClientID, session: Session): State = {
    val newSessions = sessions + ((clientID, session))
    this.copy(sessions = newSessions)
  }
  
  override def setChannel(clientID: ClientID, channel: Channel): State = {
    val state = for {
      s <- sessionFromClientID(clientID)
      newSession = s.copy(channel = Some(channel))
      newState = setSession(clientID, newSession)
    } yield newState
    state.getOrElse(this)
  }
  
  override def addClosingChannel(channel: Channel, packets: Seq[Packet]): State = {
    val newClosing = closing + ((channel, packets))
    this.copy(closing = newClosing)
  }
  
  override def updateSession(clientID: ClientID, f: Session => Session): State = {
    this.sessionFromClientID(clientID)
      .fold[State](this)(ses => {
        val newSession = f(ses)
        this.setSession(clientID, newSession)
      })
  }
  
  override def deleteSession(clientID: ClientID): State = {
    val newSessions = sessions - clientID
    this.copy(sessions = newSessions)
  }
  
  
  override def setWillMessage(channel: Channel, willMessage: Packet.ApplicationMessage): State = {
    val newWills = wills + ((channel, willMessage))
    this.copy(wills = newWills)
  }
  
  override def deleteWillMessage(channel: Channel): State = {
    val newWills = wills - channel
    this.copy(wills = newWills)
  }
  
  override def takeAllPendingTransmission: (State, Map[Channel, Seq[Packet]]) = {
    object ActiveSession {
      def unapply(session: Session): Option[Channel] = session.channel
    }
  
    val pending = sessions.collect { case (_, s @ ActiveSession(channel)) => (channel, s.pendingTransmission) }
  
    val newSessions = sessions.map {
      case (id, s @ ActiveSession(_)) => id -> s.copy(pendingTransmission = Seq())
      case (id, s) => id -> s
    }
  
    val newState = this.copy(sessions = newSessions)
  
    (newState, pending)
  }
  
  override def takeClosing: (State, Map[Channel, Seq[Packet]]) = (this.copy(closing = Map()), this.closing)
  
  override def setRetainMessage(topic: Topic, message: ApplicationMessage): State = {
    val newRetains = retains + ((topic, message))
    this.copy(retains = newRetains)
  }
  
  override def clearRetainMessage(topic: Topic): State = {
    val newRetains = retains - topic
    this.copy(retains = newRetains)
  }
}
