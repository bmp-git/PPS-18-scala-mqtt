package mqtt.broker

import mqtt.broker.Common.closeChannel
import mqtt.broker.StateImplicits.functionToStateTransitionWithError
import mqtt.broker.Violation.{InvalidIdentifier, InvalidProtocolName, InvalidProtocolVersion, MultipleConnectPacketsOnSameChannel}
import mqtt.model.Packet.ConnectReturnCode.ConnectionAccepted
import mqtt.model.Packet.{ApplicationMessage, Connack, Connect, Protocol}

import scala.concurrent.duration.Duration

/**
 * Handles connect packets.
 */
object ConnectPacketHandler extends PacketHandler[Connect] {
  
  override def handle(state: State, packet: Connect, channel: Channel): State = {
    {
      for {
        _ <- checkNotFirstPacketOfChannel(channel)
        _ <- checkProtocol(packet.protocol)
        _ <- checkClientId(packet.clientId)
        _ <- disconnectOtherConnected(packet.clientId)
        sessionPresent <- manageSession(packet.clientId, packet.cleanSession)
        _ <- setCleanSessionFlag(packet.clientId, cleanSession = packet.cleanSession)
        _ <- updateChannel(packet.clientId, channel)
        _ <- setWillMessage(channel, packet.willMessage)
        _ <- setKeepAlive(packet.clientId, packet.keepAlive)
        _ <- replyWithACK(packet.clientId, sessionPresent)
      } yield ()
    } run state match {
      //TODO remove println, use a logger
      case Left(v) => println(v.toString); v.handle(channel)(state) //close connection in case of error
      case Right((_, s)) => s
    }
  }
  
  
  /**
   * Checks if there is already a session bound with a channel.
   * If there is, this is not the first connect packet received on the channel.
   *
   * @param channel the channel to check.
   * @return a function that maps a state to a violation or to a new state.
   */
  def checkNotFirstPacketOfChannel(channel: Channel): State => Either[Violation, (Unit, State)] = state => {
    // check duplicate connect 3.1.0-2
    state.sessionFromChannel(channel).fold[Either[Violation, (Unit, State)]](Right((), state))(_ => {
      //there is already a session with this channel
      Left(MultipleConnectPacketsOnSameChannel())
    })
  }
  
  /**
   * Checks if the protocol is supported.
   *
   * @param protocol the protocol information.
   * @return a function that maps a state to a violation or to a new state.
   */
  def checkProtocol(protocol: Protocol): State => Either[Violation, (Unit, State)] = state => {
    val f = for {
      _ <- checkProtocolName(protocol.name)
      _ <- checkProtocolVersion(protocol.level)
    } yield ()
    f.run(state)
  }
  
  /**
   * Checks if the protocol name is supported.
   *
   * @param name the protocol name.
   * @return a function that maps a state to a violation or to a new state.
   */
  def checkProtocolName(name: String): State => Either[Violation, (Unit, State)] = state => {
    if (name != "MQTT") Left(InvalidProtocolName()) else Right((), state)
  }
  
  /**
   * Checks if the protocol version is supported.
   *
   * @param version the protocol version.
   * @return a function that maps a state to a violation or to a new state.
   */
  def checkProtocolVersion(version: Int): State => Either[Violation, (Unit, State)] = state => {
    if (version != 4) Left(InvalidProtocolVersion()) else Right((), state)
  }
  
  /**
   * Checks if the client identifier is legit.
   *
   * @param clientId the client identifier.
   * @return a function that maps a state to a violation or to a new state.
   */
  def checkClientId(clientId: String): State => Either[Violation, (Unit, State)] = state => {
    if (clientId.isEmpty || clientId.length > 23) Left(InvalidIdentifier()) else Right((), state)
  }
  
  /**
   * Checks if the session bound to a client identifier, if present, has a bounded channel.
   * If it has, there is another client connected with the same client identifier that must be disconnected.
   *
   * @param clientId the client identifier.
   * @return a function that maps a state to a violation or to a new state.
   */
  def disconnectOtherConnected(clientId: String): State => Either[Violation, (Unit, State)] = state => {
    //disconnect if already connected
    //there is a session with the same clientID and a non-empty channel.
    Right((), state.sessionFromClientID(clientId).fold(state)(sess => sess.channel.fold(state)(sk => {
      closeChannel(sk)(state)
    })))
  }
  
  /**
   * Decides if the session must be cleared or recovered, given the cleanSession flag.
   *
   * @param clientId     the client identifier associated with the session
   * @param cleanSession the cleanSession flag.
   * @return a function that maps a state to a violation or to a tuple containing
   *         the new state and the flag that tells if the session has been successfully recovered or not.
   */
  def manageSession(clientId: String, cleanSession: Boolean): State => Either[Violation, (Boolean, State)] = state => {
    if (cleanSession) createSession(clientId)(state) else recoverSession(clientId)(state)
  }
  
  /**
   * Recovers the session if present (does nothing, only sets the flag) or creates a new one if not.
   *
   * @param clientId the client identifier.
   * @return a function that maps a state to a violation or to a tuple containing
   *         the new state and the flag that tells if the session has been successfully recovered or not.
   */
  def recoverSession(clientId: String): State => Either[Violation, (Boolean, State)] = state => {
    //session present 1 in connack
    state.sessionFromClientID(clientId).fold(createSession(clientId)(state))(_ => Right((true, state)))
  }
  
  /**
   * Creates a new empty session for the given client identifier.
   *
   * @param clientId the client identifier.
   * @return a function that maps a state to a violation or to a tuple containing
   *         the new state and the flag that tells that the session has not been recovered.
   */
  def createSession(clientId: String): State => Either[Violation, (Boolean, State)] = state => {
    //session present 0 in connack
    Right((false, state.setSession(clientId, session = Session.createEmptySession())))
  }
  
  /**
   * Sets the persistent flag of the session accordingly to the cleanSession flag received.
   *
   * @param clientId     the client identifier.
   * @param cleanSession the cleanSession flag received.
   * @return a function that maps a state to a violation or to a new state.
   */
  def setCleanSessionFlag(clientId: String, cleanSession: Boolean): State => Either[Violation, (Unit, State)] = state => {
    Right((), state.updateSession(clientId, s => s.copy(persistent = !cleanSession)))
  }
  
  /**
   * Sets the channel of the session identified by the client identifier.
   * After this call the session will be considered as connected.
   *
   * @param clientId the client identifier.
   * @param channel  the channel to be bound.
   * @return a function that maps a state to a violation or to a new state.
   */
  def updateChannel(clientId: String, channel: Channel): State => Either[Violation, (Unit, State)] = state => {
    Right((), state.setChannel(clientId, channel))
  }
  
  /**
   * Associates a will message to the channel.
   *
   * @param channel     the channel.
   * @param willMessage the will message.
   * @return a function that maps a state to a violation or to a new state.
   */
  def setWillMessage(channel: Channel, willMessage: Option[ApplicationMessage]): State => Either[Violation, (Unit, State)] = state => {
    Right((), willMessage.fold(state)(m => state.setWillMessage(channel, m)))
  }
  
  /**
   * Sets the keep alive attribute of the session identified by the client identifier.
   *
   * @param clientId  the client identifier.
   * @param keepAlive the keep alive timespan to be set.
   * @return a function that maps a state to a violation or to a new state.
   */
  def setKeepAlive(clientId: String, keepAlive: Duration): State => Either[Violation, (Unit, State)] = state => {
    val newState = state.updateSession(clientId, s => {
      s.copy(keepAlive = keepAlive)
    })
    Right((), newState)
  }
  
  /**
   * Creates the CONNACK setting the session present flag specified.
   * Adds the created message to the pendingTransmissions of the session identified by the client identifier.
   *
   * @param clientId       the client identifier.
   * @param sessionPresent the session present flag.
   * @return a function that maps a state to a violation or to a new state.
   */
  def replyWithACK(clientId: String, sessionPresent: Boolean): State => Either[Violation, (Unit, State)] = state => {
    val newState = state.updateSession(clientId, s => {
      val newPending = s.pendingTransmission ++ Seq(Connack(sessionPresent, ConnectionAccepted))
      s.copy(pendingTransmission = newPending)
    })
    Right((), newState)
  }
  
}
