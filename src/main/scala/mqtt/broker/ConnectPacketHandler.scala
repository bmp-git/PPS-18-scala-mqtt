package mqtt.broker

import mqtt.broker.Violation.{GenericViolation, InvalidIdentifier, InvalidProtocolVersion}
import mqtt.Socket
import mqtt.model.Packet.ConnectReturnCode.{ConnectionAccepted}
import mqtt.model.Packet.{ApplicationMessage, Connack, Connect, Protocol}

import scala.concurrent.duration.Duration

object ConnectPacketHandler extends PacketHandler[Connect] {
  override def handle(state: State, packet: Connect, socket: Socket): State = {
    (for {
      _ <- checkProtocol(packet.protocol)
      _ <- checkClientId(packet.clientId)
      s0 <- manageSession(packet.clientId, packet.cleanSession)(state)
      sessionPresent = s0._1 //TODO correct workaround
      s1 <- updateSocket(packet.clientId, socket)(s0._2)
      s2 <- setWillMessage(packet.clientId, packet.willMessage)(s1)
      s3 <- setKeepAlive(packet.clientId, packet.keepAlive)(s2)
      s4 <- replyWithACK(packet.clientId, sessionPresent)(s3)
    } yield s4) match {
      case Left(v) => println(v.msg); v.handle(socket)(state) //close connection in case of error
      case Right(s) => s
    }
  }
  
  //TODO check duplicate connect
  
  //TODO check socket is not disconnected
  
  //TODO publish will packet on protocol violation
  
  
  def checkProtocol(protocol: Protocol): Either[Violation, Unit] = {
    for {
      _ <- checkProtocolName(protocol.name)
      _ <- checkProtocolVersion(protocol.level)
    } yield ()
  }
  
  def checkProtocolName(name: String): Either[Violation, Unit] = {
    if (name != "MQTT") Left(InvalidProtocolVersion()) else Right(())
  }
  
  def checkProtocolVersion(version: Int): Either[Violation, Unit] = {
    if (version != 4) Left(InvalidProtocolVersion()) else Right(())
  }
  
  def checkClientId(clientId: String): Either[Violation, Unit] = {
    if (clientId.isEmpty || clientId.length > 23) Left(InvalidIdentifier()) else Right(())
  }
  
  def createSession(clientId: String)(state: State): Either[Violation, (Boolean, State)] = {
    //session present 0 in connack
    Right((false, state.setSession(clientId, session = Session.createEmptySession())))
  }
  
  def recoverSession(clientId: String)(state: State): Either[Violation, (Boolean, State)] = {
    //session present 1 in connack
    state.sessionFromClientID(clientId).fold(createSession(clientId)(state))(_ => Right((true, state)))
  }
  
  //Boolean true if session was present
  def manageSession(clientId: String, cleanSession: Boolean)(state: State): Either[Violation, (Boolean, State)] = {
    if (cleanSession) createSession(clientId)(state) else recoverSession(clientId)(state)
  }
  
  def updateSocket(clientId: String, socket: Socket)(state: State): Either[Violation, State] = {
    //disconnect if already connected
    val s1 = state.sessionFromClientID(clientId).fold(state)(sess => sess.socket.fold(state)(sk => state.addClosingChannel(sk, Seq())))
    Right(s1.setSocket(clientId, socket))
  }
  
  def setWillMessage(clientId: String, willMessage: Option[ApplicationMessage])(state: State): Either[Violation, State] = {
    state.sessionFromClientID(clientId)
      .fold[Either[Violation, State]](Left(GenericViolation("Session not found during will set")))(s => {
        Right(state.setSession(clientId, s.copy(willMessage = willMessage)))})
  }
  
  def setKeepAlive(clientId: String, keepAlive: Duration)(state: State): Either[Violation, State] = {
    state.sessionFromClientID(clientId)
      .fold[Either[Violation, State]](Left(GenericViolation("Session not found during keep alive set")))(s => {
        Right(state.setSession(clientId, s.copy(keepAlive = keepAlive)))})
  }
  
  def replyWithACK(clientId: String, sessionPresent: Boolean)(state: State): Either[Violation, State] = {
    state.sessionFromClientID(clientId)
      .fold[Either[Violation, State]](Left(GenericViolation("Session not found for ACK")))(s => {
        val new_pending = s.pendingTransmission ++ Seq(Connack(sessionPresent, ConnectionAccepted))
        Right(state.setSession(clientId, s.copy(pendingTransmission = new_pending)))
      })
  }
  
}
