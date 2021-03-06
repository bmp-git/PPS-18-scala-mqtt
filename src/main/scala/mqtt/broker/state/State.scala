package mqtt.broker.state

import mqtt.model.Packet.ApplicationMessage
import mqtt.model.Types.ClientID
import mqtt.model.{BrokerConfig, Packet, Topic}

/**
 * Represents the internal State of the server (client sessions, retain messages, state of the channels).
 * Every field is immutable therefore every operation that modifies the state returns an instance of the new one.
 */
trait State {
  /**
   * @return a map containing the session relative a to a specific ClientID.
   */
  def sessions: Map[ClientID, Session]
  
  /**
   * @return a map containing the retain message relative a to a specific Topic.
   */
  def retains: Map[Topic, ApplicationMessage]
  
  /**
   * @return a map containing the channels that need to be closed, optionally sending some packets before closing.
   */
  def closing: Map[Channel, Seq[Packet]]
  
  /**
   * @return a map containing the will message associated to a specific channel.
   */
  def wills: Map[Channel, ApplicationMessage]
  
  /**
   * @return a map containing the users credentials (username -> sha256(password)), password is optional.
   */
  def credentials: Map[String, Option[String]]
  
  /**
   * @return the configuration of the broker.
   */
  def config: BrokerConfig
  
  /**
   * Adds a channel to the Map containing the channels that need to be closed.
   *
   * @param channel the channel that needs to be closed.
   * @param packets the packets to send on the channel before closing it.
   * @return the new State.
   */
  def addClosingChannel(channel: Channel, packets: Seq[Packet]): State
  
  /**
   * Gets a session of a client from his client identifier.
   * @param clientID the client identifier.
   * @return the session if found.
   */
  def sessionFromClientID(clientID: ClientID): Option[Session]
  
  /**
   * Gets a session of a client from his channel, if present.
   *
   * @param channel the channel associated to the client session.
   * @return the session and the relative client identifier if found.
   */
  def sessionFromChannel(channel: Channel): Option[(ClientID, Session)]
  
  /**
   * Sets a session of a client.
   * @param clientID the client identifier that will be associated with the session.
   * @param session the session to be saved.
   * @return the new State.
   */
  def setSession(clientID: ClientID, session: Session): State
  
  /**
   * Sets the channel relative to a session of a client.
   *
   * @param clientID the client identifier to identify the session.
   * @param channel  the channel to be set.
   * @return the new State.
   */
  def setChannel(clientID: ClientID, channel: Channel): State
  
  /**
   * Updates a session of a client (if found), given the mapping function.
   *
   * @param clientID the client identifier to identify the session.
   * @param f the function that updates the session.
   * @return the new State.
   */
  def updateSessionFromClientID(clientID: ClientID, f: Session => Session): State
  
  /**
   * Updates a session of a client (if found), given the mapping function.
   *
   * @param channel the channel to identify the session.
   * @param f       the function that updates the session.
   * @return the new State.
   */
  def updateSessionFromChannel(channel: Channel, f: Session => Session): State
  
  /**
   * Deletes a session of a client.
   * @param clientID the client identifier to identify the session.
   * @return the new State.
   */
  def deleteSession(clientID: ClientID): State
  
  /**
   * Sets the will message relative to a specified channel.
   *
   * @param channel     the channel to which associate the will message.
   * @param willMessage the will message associated to the channel.
   * @return the new State.
   */
  def setWillMessage(channel: Channel, willMessage: ApplicationMessage): State
  
  /**
   * Deletes the will message associated to a specified channel.
   *
   * @param channel the channel to which the will message is associated.
   * @return the new State.
   */
  def deleteWillMessage(channel: Channel): State
  
  /**
   * Takes the pending transmissions from all the currently active client sessions.
   * The pending messages are removed from the client sessions.
   *
   * @return a map containing the packets to send for each channel.
   */
  def takeAllPendingTransmission: (State, Map[Channel, Seq[Packet]])
  
  /**
   * Takes all the connections that must be closed, removing them from the state.
   *
   * @return a map containing, for each channel to be closed, the sequence of packets to be sent before closing.
   */
  def takeClosing: (State, Map[Channel, Seq[Packet]])
  
  /**
   * Sets the retain message relative to a specified topic.
   *
   * @param topic   the topic to which associate the will message.
   * @param message the retain message associated to the topic.
   * @return the new State.
   */
  def setRetainMessage(topic: Topic, message: ApplicationMessage): State
  
  /**
   * Deletes the retain message associated to a specified topic.
   *
   * @param topic the topic to which the retain message is associated.
   * @return the new State.
   */
  def clearRetainMessage(topic: Topic): State
}
