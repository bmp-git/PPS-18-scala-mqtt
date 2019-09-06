package mqtt.broker

import mqtt.model.Packet
import mqtt.model.Packet.ApplicationMessage

/**
 * Contains common utility methods to modify the state of the server.
 */
object Common {
  /**
   * Closes a socket, publishes the will message if present and updates or deletes the client session.
   *
   * @param socket the socket to be closed.
   * @return a function that maps the old server state in the new one.
   */
  def closeSocket(socket: Socket): State => State = state => {
    closeSocketWithPackets(socket, Seq())(state)
  }
  
  /**
   * Closes a socket, publishes the will message if present and updates or deletes the client session.
   *
   * @param socket       the socket to be closed.
   * @param closePackets the packets to be sent before closing.
   * @return a function that maps the old server state in the new one.
   */
  def closeSocketWithPackets(socket: Socket, closePackets: Seq[Packet]): State => State = state => {
    (publishWillMessage(socket) andThen closeSocketNoWillPublish(socket, closePackets)) (state)
  }
  
  /**
   * Closes a socket and updates or deletes the client session
   *
   * @param socket       the socket to be closed.
   * @param closePackets the packets to be sent before closing.
   * @return a function that maps the old server state in the new one.
   */
  def closeSocketNoWillPublish(socket: Socket, closePackets: Seq[Packet]): State => State = state => {
    updateSessionAfterSocketDisconnection(socket)(state).addClosingChannel(socket.setWillMessage(Option.empty), closePackets)
  }
  
  /**
   * Updates or deletes the client session relative to the socket, if present.
   * If the session is persistent, the session is updated removing the socket.
   * If the session is not persistent, the session is removed from the server state.
   *
   * @return a function that maps the old server state in the new one.
   */
  def updateSessionAfterSocketDisconnection(socket: Socket): State => State = state => {
    state.sessionFromSocket(socket).fold(state) { case (id, sess) => {
      if (sess.persistent) {
        val newSess = sess.copy(socket = Option.empty)
        state.updateSession(id, _ => newSess)
      } else {
        state.deleteSession(id)
      }
    }
    }
  }
  
  /**
   * Publishes the will message related to a channel if present.
   *
   * @return a function that maps the old server state in the new one.
   */
  def publishWillMessage(socket: Socket): State => State = state => {
    socket.willMessage.fold[State](state)(publishMessage(_)(state))
  }
  
  /**
   * Publishes a message.
   *
   * @param message the message to publish.
   * @return a function that maps the old server state in the new one.
   */
  def publishMessage(message: ApplicationMessage): State => State = state => {
    //TODO
    println("Message published ".concat(message.toString))
    state
  }
  
}
