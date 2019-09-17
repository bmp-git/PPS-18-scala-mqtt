package mqtt.broker

import mqtt.model.Packet.ApplicationMessage
import mqtt.model.Types.ClientID
import mqtt.model.{Packet, Types}

/**
 * Represents the internal state of the server/broker.
 *
 * @param sessions the client sessions to be initialized with.
 * @param retains  the retain messages to be initialized with.
 * @param closing  the closing channels to be initialized with.
 * @param wills    the will messages to be initialized with.
 */
case class BrokerState(override val sessions: Map[ClientID, Session],
                       override val retains: Map[Types.Topic, Packet.ApplicationMessage],
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
  
  
  //TODO add tests for these last two methods
  
  override def setWillMessage(channel: Channel, willMessage: Packet.ApplicationMessage): State = {
    val newWills = wills + ((channel, willMessage))
    this.copy(wills = newWills)
  }
  
  override def deleteWillMessage(channel: Channel): State = {
    val newWills = wills - channel
    this.copy(wills = newWills)
  }
  
  override def takeAllPendingTransmission: (State, Map[Channel, Seq[Packet]]) = {
    //TODO: Refactor
    val pt = sessions.filter(_._2.channel.isDefined).map(a => (a._2.channel.head, a._2.pendingTransmission))
    
    val ns = this.copy(sessions = sessions.filter(_._2.channel.isDefined).map(a => a._1 -> a._2.copy(pendingTransmission = Seq())) ++
      sessions.filter(_._2.channel.isEmpty))
    
    (ns, pt)
  }
}
