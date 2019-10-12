package mqtt.broker.handlers

import mqtt.broker.Common
import mqtt.broker.Common.{closeChannel, sendPacketToClient, updateLastContact}
import mqtt.broker.state.StateImplicits._
import mqtt.broker.state.Violation._
import mqtt.broker.state.{Channel, Session, State, Violation}
import mqtt.model.Packet.ConnectReturnCode.ConnectionAccepted
import mqtt.model.Packet.{Connack, Connect, Credential}
import mqtt.model.Topic
import mqtt.model.Types.Password

/**
 * Represents an handler for connect packets.
 *
 * @param packet  the connect packet to handle.
 * @param channel the channel on which the packet has been received.
 */
case class ConnectPacketHandler(override val packet: Connect, override val channel: Channel) extends PacketHandler[Connect] with AutoViolationHandler {
  
  override def handle: State => State = {
    for {
      _ <- checkIsFirstPacketOfChannel
      _ <- checkProtocol
      _ <- handleCredentials
      _ <- checkClientId
      _ <- checkWillMessageTopic
      _ <- disconnectOtherConnected
      sessionPresent <- manageSession
      _ <- storeCleanSessionFlag
      _ <- storeChannel
      _ <- storeWillMessage
      _ <- storeKeepAlive
      _ <- replyWithACK(sessionPresent)
      _ <- updateLastContact(channel)
      } yield ()
  }
  
  
  /**
   * Checks if there is already a session bound with the channel.
   * If there is, this is not the first connect packet received on the channel.
   *
   * @return a function that maps a state to a violation or to a new state.
   */
  def checkIsFirstPacketOfChannel: State => Either[Violation, State] = state => {
    // check duplicate connect 3.1.0-2
    state.sessionFromChannel(channel).fold[Either[Violation, State]](Right(state))(_ => {
      //there is already a session with this channel
      Left(MultipleConnectPacketsOnSameChannel)
    })
  }
  
  /**
   * Checks if the protocol is supported.
   *
   * @return a function that maps a state to a violation or to a new state.
   */
  def checkProtocol: State => Either[Violation, State] = state => {
    val f = for {
      _ <- checkProtocolName(packet.protocol.name)
      _ <- checkProtocolVersion(packet.protocol.level)
    } yield ()
    f.run(state)
  }
  
  /**
   * Checks if the protocol name is supported.
   *
   * @param name the protocol name.
   * @return a function that maps a state to a violation or to a new state.
   */
  def checkProtocolName(name: String): State => Either[Violation, State] = state => {
    if (name != "MQTT") Left(InvalidProtocolName) else Right(state)
  }
  
  /**
   * Checks if the protocol version is supported.
   *
   * @param version the protocol version.
   * @return a function that maps a state to a violation or to a new state.
   */
  def checkProtocolVersion(version: Int): State => Either[Violation, State] = state => {
    if (version != 4) Left(InvalidProtocolVersion) else Right(state)
  }
  
  
  /**
   * Handles the credentials of the packet. If the server allows anonymous access the credentials are not checked.
   *
   * @return a function that maps a state to a violation or to a new state.
   */
  def handleCredentials: State => Either[Violation, State] = state => {
    if (state.config.allowAnonymous) Right(state) else checkCredentials(state)
  }
  
  /**
   * Gets the client credentials specified in the packet. Fails if the credentials are not specified.
   *
   * @return a violation or the client credentials.
   */
  def getClientCredentials: Either[Violation, Credential] = {
    packet.credential.fold[Either[Violation, Credential]](Left(ClientNotAuthorized))(pwOpt => Right(pwOpt))
  }
  
  /**
   * Gets the stored password relative to a username. Fails if the record relative to that username does not exist.
   *
   * @return a function that maps a state to a violation or to the stored password.
   */
  def getStoredPassword(username: String): State => Either[Violation, Option[String]] = state => {
    state.credentials.get(username).fold[Either[Violation, Option[String]]](Left(ClientNotAuthorized))(pwOpt => Right(pwOpt))
  }
  
  /**
   * Compares the specified client password with the stored password.
   *
   * @param clientPassword the client password.
   * @param storedPassword the stores password.
   * @return a Left[Violation] if the passwords do not match, a Right otherwise.
   */
  def comparePasswords(clientPassword: Option[Password], storedPassword: Option[String]): Either[Violation, Unit] = {
    (clientPassword.map(Common.sha256), storedPassword) match {
      case (None, None) => Right(())
      case (Some(p1), Some(p2)) if p1 == p2 => Right(())
      case _ => Left(ClientNotAuthorized)
    }
  }
  
  /**
   * Checks if the credentials specified by the client are compatible with the credentials stored on the server.
   *
   * @return a function that maps a state to a violation or to a new state.
   */
  def checkCredentials: State => Either[Violation, State] = state => {
    for {
      credentials <- getClientCredentials
      storedPass <- getStoredPassword(credentials.username)(state)
      _ <- comparePasswords(credentials.password, storedPass)
    } yield state
  }
  
  /**
   * Checks if the client identifier is legit.
   *
   * @return a function that maps a state to a violation or to a new state.
   */
  def checkClientId: State => Either[Violation, State] = state => {
    val clientId = packet.clientId
    if (clientId.isEmpty || clientId.length > 23) Left(InvalidIdentifier) else Right(state)
  }
  
  /**
   * Checks if the will message topic is a valid topic.
   *
   * @return a function that maps a state to a violation or to a new state.
   */
  def checkWillMessageTopic: State => Either[Violation, State] = state => {
    packet.willMessage.fold[Either[Violation, State]](Right(state))(m => {
      if (Topic.valid(m.topic)) Right(state) else Left(InvalidWillTopic)
    })
  }
  
  /**
   * Checks if the session bound to a client identifier, if present, has a bounded channel.
   * If it has, there is another client connected with the same client identifier that must be disconnected.
   *
   * @return a function that maps a state to a new state.
   */
  def disconnectOtherConnected: State => State = state => {
    //disconnect if already connected
    //there is a session with the same clientID and a non-empty channel.
    state.sessionFromClientID(packet.clientId).fold(state)(sess => sess.channel.fold(state)(sk => {
      closeChannel(sk)(state)
    }))
  }
  
  /**
   * Decides if the session must be cleared or recovered, considering the cleanSession flag.
   *
   * @return a function that maps a state to a tuple containing
   *         the new state and the flag that tells if the session has been successfully recovered or not.
   */
  def manageSession: State => (Boolean, State) = state => {
    if (packet.cleanSession) createSession(state) else recoverSession(state)
  }
  
  /**
   * Recovers the session if present (does nothing, only sets the flag) or creates a new one if not.
   *
   * @return a function that maps a state to a tuple containing
   *         the new state and the flag that tells if the session has been successfully recovered or not.
   */
  def recoverSession: State => (Boolean, State) = state => {
    //session present 1 in connack
    state.sessionFromClientID(packet.clientId).fold(createSession(state))(_ => (true, state))
  }
  
  /**
   * Creates a new empty session for the given client identifier.
   *
   * @return a function that maps a state to a tuple containing
   *         the new state and the flag that tells that the session has not been recovered.
   */
  def createSession: State => (Boolean, State) = state => {
    //session present 0 in connack
    (false, state.setSession(packet.clientId, session = Session.createEmptySession()))
  }
  
  /**
   * Sets the persistent flag of the session accordingly to the cleanSession flag received.
   *
   * @return a function that maps a state to a new state.
   */
  def storeCleanSessionFlag: State => State = state => {
    state.updateSessionFromClientID(packet.clientId, s => s.copy(persistent = !packet.cleanSession))
  }
  
  /**
   * Sets the channel of the session identified by the client identifier.
   * After this call the session will be considered as connected.
   *
   * @return a function that maps a state to a new state.
   */
  def storeChannel: State => State = state => {
    state.setChannel(packet.clientId, channel)
  }
  
  /**
   * Associates the will message to the channel if present.
   *
   * @return a function that maps a state to a new state.
   */
  def storeWillMessage: State => State = state => {
    packet.willMessage.fold(state)(m => state.setWillMessage(channel, m))
  }
  
  /**
   * Sets the keep alive attribute of the session identified by the client identifier.
   *
   * @return a function that maps a state to a new state.
   */
  def storeKeepAlive: State => State = state => {
    state.updateSessionFromClientID(packet.clientId, s => {
      s.copy(keepAlive = packet.keepAlive)
    })
  }
  
  /**
   * Creates the CONNACK setting the session present flag specified.
   * Adds the created message to the pendingTransmissions of the session identified by the client identifier.
   *
   * @param sessionPresent the session present flag.
   * @return a function that maps a state to a new state.
   */
  def replyWithACK(sessionPresent: Boolean): State => State = sendPacketToClient(packet.clientId, Connack(sessionPresent, ConnectionAccepted))
  
}
