package mqtt.broker

import mqtt.model.Packet.ApplicationMessage
import mqtt.model.Types.ClientID
import mqtt.model.{Packet, Types}

/**
 * Represents the internal state of the server/broker.
 *
 * @param sessions the client sessions to be initialized with.
 * @param retains  the retain messages to be initialized with.
 * @param closing  the closing sockets to be initialized with.
 * @param wills    the will messages to be initialized with.
 */
case class BrokerState(override val sessions: Map[ClientID, Session],
                       override val retains: Map[Types.Topic, Packet.ApplicationMessage],
                       override val closing: Map[Socket, Seq[Packet]],
                       override val wills: Map[Socket, ApplicationMessage]) extends State {
  override def sessionFromClientID(clientID: ClientID): Option[Session] = sessions.get(clientID)
  
  override def sessionFromSocket(socket: Socket): Option[(ClientID, Session)] = {
    sessions.collectFirst { case (id, s) if s.socket.fold(false)(_ == socket) => (id, s) }
  }
  
  override def setSession(clientID: ClientID, session: Session): State = {
    val newSessions = sessions + ((clientID, session))
    this.copy(sessions = newSessions)
  }
  
  override def setSocket(clientID: ClientID, socket: Socket): State = {
    val state = for {
      s <- sessionFromClientID(clientID)
      newSession = s.copy(socket = Some(socket))
      newState = setSession(clientID, newSession)
    } yield newState
    state.getOrElse(this)
  }
  
  override def addClosingChannel(socket: Socket, packets: Seq[Packet]): State = {
    val newClosing = closing + ((socket, packets))
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
  
  override def setWillMessage(socket: Socket, willMessage: Packet.ApplicationMessage): State = {
    val newWills = wills + ((socket, willMessage))
    this.copy(wills = newWills)
  }
  
  override def deleteWillMessage(socket: Socket): State = {
    val newWills = wills - socket
    this.copy(wills = newWills)
  }
}
