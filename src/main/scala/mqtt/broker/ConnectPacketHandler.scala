package mqtt.broker

import mqtt.broker.StateImplicits.StateTransitionWithError_Implicit
import mqtt.broker.Violation.{GenericViolation, InvalidIdentifier, InvalidProtocolVersion}
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
      _ <- updateSocket(packet.clientId, socket)
      _ <- setWillMessage(packet.clientId, packet.willMessage)
      _ <- setKeepAlive(packet.clientId, packet.keepAlive)
      _ <- replyWithACK(packet.clientId, sessionPresent)
    } yield ()
    
    f.run(state) match {
      case Left(v) => println(v.msg); v.handle(socket)(state) //close connection in case of error
      case Right((_, s)) => s
    }
  }
  
  //TODO publish will packet on protocol violation
  
  //TODO move this in the upper layer
  def checkSocketNotInClosing(socket: Socket): State => Either[Violation, (Unit, State)] = state => {
    state.closing.get(socket).fold[Either[Violation, (Unit, State)]](Right((), state))(_ => {
      Left(GenericViolation("Received a packet on a closing socket"))
    })
  }
  
  def checkNotFirstPacketOfSocket(socket: Socket): State => Either[Violation, (Unit, State)] = state => {
    // check duplicate connect 3.1.0-2
    state.sessionFromSocket(socket).fold[Either[Violation, (Unit, State)]](Right((), state))(_ => {
      Left(GenericViolation("Received two connect packets on same socket"))
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
    if (name != "MQTT") Left(InvalidProtocolVersion()) else Right((), state)
  }
  
  def checkProtocolVersion(version: Int): State => Either[Violation, (Unit, State)] = state => {
    if (version != 4) Left(InvalidProtocolVersion()) else Right((), state)
  }
  
  def checkClientId(clientId: String): State => Either[Violation, (Unit, State)] = state => {
    if (clientId.isEmpty || clientId.length > 23) Left(InvalidIdentifier()) else Right((), state)
  }
  
  def disconnectOtherConnected(clientId: String): State => Either[Violation, (Unit, State)] = state => {
    //disconnect if already connected
    Right((), state.sessionFromClientID(clientId).fold(state)(sess => sess.socket.fold(state)(sk => state.addClosingChannel(sk, Seq()))))
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
  
  def updateSocket(clientId: String, socket: Socket): State => Either[Violation, (Unit, State)] = state => {
    Right((), state.setSocket(clientId, socket))
  }
  
  def setWillMessage(clientId: String, willMessage: Option[ApplicationMessage]): State => Either[Violation, (Unit, State)] = state => {
    state.sessionFromClientID(clientId)
      .fold[Either[Violation, (Unit, State)]](Left(GenericViolation("Session not found during will set")))(s => {
        Right((), state.setSession(clientId, s.copy(willMessage = willMessage)))
      })
  }
  
  def setKeepAlive(clientId: String, keepAlive: Duration): State => Either[Violation, (Unit, State)] = state => {
    state.sessionFromClientID(clientId)
      .fold[Either[Violation, (Unit, State)]](Left(GenericViolation("Session not found during keep alive set")))(s => {
        Right((), state.setSession(clientId, s.copy(keepAlive = keepAlive)))
      })
  }
  
  def replyWithACK(clientId: String, sessionPresent: Boolean): State => Either[Violation, (Unit, State)] = state => {
    state.sessionFromClientID(clientId)
      .fold[Either[Violation, (Unit, State)]](Left(GenericViolation("Session not found for ACK")))(s => {
        val new_pending = s.pendingTransmission ++ Seq(Connack(sessionPresent, ConnectionAccepted))
        Right((), state.setSession(clientId, s.copy(pendingTransmission = new_pending)))
      })
  }
  
}
