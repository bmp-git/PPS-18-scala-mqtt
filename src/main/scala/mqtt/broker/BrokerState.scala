package mqtt.broker

import mqtt.model.Types.ClientID
import mqtt.model.{Packet, Types}
import mqtt.{Session, Socket, State}

case class BrokerState(override val sessions: Map[ClientID, Session], override val retains: Map[Types.Topic, Packet.ApplicationMessage]) extends State {
  override def sessionFromClientID(clientID: ClientID): Option[Session] = sessions.get(clientID)
  
  override def sessionFromSocket(socket: Socket): Option[Session] = sessions.collectFirst { case (_, s) if s.socket.fold(false)(_ == socket) => s }
  
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
  
  
}
