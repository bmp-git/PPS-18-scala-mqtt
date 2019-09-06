package mqtt.broker

import mqtt.model.Packet
import mqtt.model.Packet.ApplicationMessage
import mqtt.model.Types.{ClientID, Topic}

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
   * @return a map containing the sockets that need to be closed, optionally sending some packets before closing.
   */
  def closing: Map[Socket, Seq[Packet]]
  
  /**
   *  Adds a socket to the Map containing the sockets that need to be closed.
   * @param socket the socket that needs to be closed.
   * @param packets the packets to send on the socket before closing it.
   * @return the new State.
   */
  def addClosingChannel(socket: Socket, packets: Seq[Packet]): State
  
  /**
   * Gets a session of a client from his client identifier.
   * @param clientID the client identifier.
   * @return the session if found.
   */
  def sessionFromClientID(clientID: ClientID): Option[Session]
  
  /**
   * Gets a session of a client from his socket, if present.
   * @param socket the socket associated to the client session.
   * @return the session and the relative client identifier if found.
   */
  def sessionFromSocket(socket: Socket): Option[(ClientID, Session)]
  
  /**
   * Sets a session of a client.
   * @param clientID the client identifier that will be associated with the session.
   * @param session the session to be saved.
   * @return the new State.
   */
  def setSession(clientID: ClientID, session: Session): State
  
  /**
   * Sets the socket relative to a session of a client.
   * @param clientID the client identifier to identify the session.
   * @param socket the socket to be set.
   * @return the new State.
   */
  def setSocket(clientID: ClientID, socket: Socket): State
  
  /**
   * Updates a session of a client, given the mapping function.
   * @param clientID the client identifier to identify the session.
   * @param f the function that updates the session.
   * @return the new State.
   */
  def updateSession(clientID: ClientID, f: Session => Session): State
  
  /**
   * Deletes a session of a client.
   * @param clientID the client identifier to identify the session.
   * @return the new State.
   */
  def deleteSession(clientID: ClientID): State
}
