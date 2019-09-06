package mqtt.broker

import mqtt.model.Types.ClientID
import mqtt.model.{Packet, Types}

case class BrokerState(override val sessions: Map[ClientID, Session],
                       override val retains: Map[Types.Topic, Packet.ApplicationMessage],
                       override val closing: Map[Socket, Seq[Packet]]) extends State {
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
  
  override def updateUserSession(clientID: ClientID, f: Session => Session): State = {
    this.sessionFromClientID(clientID)
      .fold[State](this)(ses => {
        val newSession = f(ses)
        this.setSession(clientID, newSession)
      })
  }
  
  override def deleteUserSession(clientID: ClientID): State = {
    val newSessions = sessions - clientID
    this.copy(sessions = newSessions)
  }
}