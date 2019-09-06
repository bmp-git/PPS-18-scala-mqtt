package mqtt.broker

import mqtt.broker.Common.closeSocket
import mqtt.broker.StateImplicits.StateTransitionWithError_Implicit
import mqtt.broker.Violation.{InvalidIdentifier, InvalidProtocolName, InvalidProtocolVersion, MultipleConnectPacketsOnSameSocket}
import mqtt.model.Packet.ConnectReturnCode.ConnectionAccepted
import mqtt.model.Packet.{ApplicationMessage, Connack, Connect, Protocol}

import scala.concurrent.duration.Duration

object ConnectPacketHandler extends PacketHandler[Connect] {
  
  override def handle(state: State, packet: Connect, socket: Socket): State = {
    val f = for {
      _ <- checkNotFirstPacketOfSocket(socket)
      _ <- checkProtocol(packet.protocol)
      _ <- checkClientId(packet.clientId)
      _ <- disconnectOtherConnected(packet.clientId)
      sessionPresent <- manageSession(packet.clientId, packet.cleanSession)
      _ <- setCleanSessionFlag(packet.clientId, cleanSession = packet.cleanSession)
      _ <- updateSocket(packet.clientId, socket)
      _ <- setWillMessage(packet.clientId, packet.willMessage)
      _ <- setKeepAlive(packet.clientId, packet.keepAlive)
      _ <- replyWithACK(packet.clientId, sessionPresent)
    } yield ()
    
    f.run(state) match {
      //TODO remove println, use a logger
      case Left(v) => println(v.toString); v.handle(socket)(state) //close connection in case of error
      case Right((_, s)) => s
    }
  }
  
  //TODO publish will packet on protocol violation
  
  
  def checkNotFirstPacketOfSocket(socket: Socket): State => Either[Violation, (Unit, State)] = state => {
    // check duplicate connect 3.1.0-2
    state.sessionFromSocket(socket).fold[Either[Violation, (Unit, State)]](Right((), state))(_ => {
      //there is already a session with this socket
      Left(MultipleConnectPacketsOnSameSocket())
    })
  }
  
  
  def checkProtocol(protocol: Protocol): State => Either[Violation, (Unit, State)] = state => {
    val f = for {
      _ <- checkProtocolName(protocol.name)
      _ <- checkProtocolVersion(protocol.level)
    } yield ()
    f.run(state)
  }
  
  def checkProtocolName(name: String): State => Either[Violation, (Unit, State)] = state => {
    if (name != "MQTT") Left(InvalidProtocolName()) else Right((), state)
  }
  
  def checkProtocolVersion(version: Int): State => Either[Violation, (Unit, State)] = state => {
    if (version != 4) Left(InvalidProtocolVersion()) else Right((), state)
  }
  
  def checkClientId(clientId: String): State => Either[Violation, (Unit, State)] = state => {
    if (clientId.isEmpty || clientId.length > 23) Left(InvalidIdentifier()) else Right((), state)
  }
  
  def disconnectOtherConnected(clientId: String): State => Either[Violation, (Unit, State)] = state => {
    //disconnect if already connected
    //there is a session with the same clientID and a non-empty socket.
    Right((), state.sessionFromClientID(clientId).fold(state)(sess => sess.socket.fold(state)(sk => {
      closeSocket(sk)(state)
    })))
  }
  
  //Boolean true if session was present
  def manageSession(clientId: String, cleanSession: Boolean): State => Either[Violation, (Boolean, State)] = state => {
    if (cleanSession) createSession(clientId)(state) else recoverSession(clientId)(state)
  }
  
  def createSession(clientId: String): State => Either[Violation, (Boolean, State)] = state => {
    //session present 0 in connack
    Right((false, state.setSession(clientId, session = Session.createEmptySession())))
  }
  
  def recoverSession(clientId: String): State => Either[Violation, (Boolean, State)] = state => {
    //session present 1 in connack
    state.sessionFromClientID(clientId).fold(createSession(clientId)(state))(_ => Right((true, state)))
  }
  
  def setCleanSessionFlag(clientId: String, cleanSession: Boolean): State => Either[Violation, (Unit, State)] = state => {
    Right((), state.updateUserSession(clientId, s => s.copy(persistent = !cleanSession)))
  }
  
  def updateSocket(clientId: String, socket: Socket): State => Either[Violation, (Unit, State)] = state => {
    Right((), state.setSocket(clientId, socket))
  }
  
  def setWillMessage(clientId: String, willMessage: Option[ApplicationMessage]): State => Either[Violation, (Unit, State)] = state => {
    val newState = state.updateUserSession(clientId, s => {
      val newSocket = s.socket.map(_.setWillMessage(willMessage))
      s.copy(socket = newSocket)
    })
    Right((), newState)
  }
  
  def setKeepAlive(clientId: String, keepAlive: Duration): State => Either[Violation, (Unit, State)] = state => {
    val newState = state.updateUserSession(clientId, s => {
      s.copy(keepAlive = keepAlive)
    })
    Right((), newState)
  }
  
  def replyWithACK(clientId: String, sessionPresent: Boolean): State => Either[Violation, (Unit, State)] = state => {
    val newState = state.updateUserSession(clientId, s => {
      val newPending = s.pendingTransmission ++ Seq(Connack(sessionPresent, ConnectionAccepted))
      s.copy(pendingTransmission = newPending)
    })
    Right((), newState)
  }
  
}
