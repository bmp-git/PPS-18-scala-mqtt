package mqtt.broker

import java.math.BigInteger
import java.security.MessageDigest
import java.util.Calendar

import mqtt.broker.handlers.PublishPacketHandler
import mqtt.broker.state.Violation.ClientIsNotConnected
import mqtt.broker.state.{Channel, Session, State, Violation}
import mqtt.model.Types.ClientID
import mqtt.model.{Packet, QoS, Topic}

/**
 * Contains common utility methods to modify the state of the server.
 */
object Common {
  /**
   * Closes a channel, publishes the will message if present and updates or deletes the client session.
   *
   * @param channel the channel to be closed.
   * @return a function that maps the old server state in the new one.
   */
  def closeChannel(channel: Channel): State => State = state => {
    closeChannelWithPackets(channel, Seq())(state)
  }
  
  /**
   * Closes a channel, publishes the will message if present and updates or deletes the client session.
   *
   * @param channel      the channel to be closed.
   * @param closePackets the packets to be sent before closing.
   * @return a function that maps the old server state in the new one.
   */
  def closeChannelWithPackets(channel: Channel, closePackets: Seq[Packet]): State => State = {
    publishWillMessage(channel) andThen closeChannelNoWillPublish(channel, closePackets)
  }
  
  /**
   * Closes a channel and updates or deletes the client session.
   * Removes the will message if present.
   *
   * @param channel      the channel to be closed.
   * @param closePackets the packets to be sent before closing.
   * @return a function that maps the old server state in the new one.
   */
  def closeChannelNoWillPublish(channel: Channel, closePackets: Seq[Packet]): State => State = {
    updateSessionAfterChannelDisconnection(channel) andThen deleteWillMessage(channel) andThen (_.addClosingChannel(channel, closePackets))
  }
  
  /**
   * Updates or deletes the client session relative to the channel, if present.
   * If the session is persistent, the session is updated removing the channel.
   * If the session is not persistent, the session is removed from the server state.
   *
   * @return a function that maps the old server state in the new one.
   */
  def updateSessionAfterChannelDisconnection(channel: Channel): State => State = state => {
    state.sessionFromChannel(channel).fold(state) { case (id, sess) => {
      if (sess.persistent) {
        val newSess = sess.copy(channel = Option.empty)
        state.updateSessionFromClientID(id, _ => newSess)
      } else {
        state.deleteSession(id)
      }
    }
    }
  }
  
  /**
   * Deletes the will message related to a channel.
   *
   * @param channel the channel.
   * @return a function that maps the old server state in the new one.
   */
  def deleteWillMessage(channel: Channel): State => State = _.deleteWillMessage(channel)
  
  /**
   * Publishes the will message related to a channel if present.
   *
   * @param channel the channel
   * @return a function that maps the old server state in the new one.
   */
  def publishWillMessage(channel: Channel): State => State = state => {
    state.wills.get(channel).fold[State](state)(msg => {
      val topic = new Topic(msg.topic) //assuming topic is valid, it should aways be
      (PublishPacketHandler.handleRetain(topic, msg) andThen PublishPacketHandler.publishMessage(msg.qos, topic, msg.payload)) (state)
    }
    )
  }
  
  /**
   * Asserts that the client has already sent a Connect packet, therefore there is a session associated with the channel.
   *
   * @param channel the channel to check whether is associated to a session.
   * @return a function that maps a state to a violation or to a tuple with the ClientID and the new state.
   */
  def assertClientConnected(channel: Channel): State => Either[Violation, (ClientID, State)] = state => {
    state.sessionFromChannel(channel).fold[Either[Violation, (ClientID, State)]](Left(ClientIsNotConnected)) { case (id, _) => Right(id, state) }
  }
  
  /**
   * Sends a packet in a specified session.
   *
   * @param packet the packet to send.
   * @return a function that maps the old session in the new one.
   */
  def sendPacket(packet: Packet): Session => Session = session => {
    val newPending = session.pendingTransmission ++ Seq(packet)
    session.copy(pendingTransmission = newPending)
  }
  
  /**
   * Sends a packet to a specified clientID.
   *
   * @param clientID the recipient of the message.
   * @param packet   the packet to send.
   * @return a function that maps the old server state in the new one.
   */
  def sendPacketToClient(clientID: ClientID, packet: Packet): State => State = state => {
    state.updateSessionFromClientID(clientID, sendPacket(packet))
  }
  
  /**
   * Sends a packet to a specified client, given his channel.
   *
   * @param channel the channel associated with the client.
   * @param packet  the packet to send.
   * @return a function that maps the old server state in the new one.
   */
  def sendPacketOnChannel(channel: Channel, packet: Packet): State => State = state => {
    state.updateSessionFromChannel(channel, sendPacket(packet))
  }
  
  /**
   * Updates the time of last contact of a client to now, if the session is active.
   *
   * @param channel the channel associated to the client.
   * @return a function that maps the old server state in the new one.
   */
  def updateLastContact(channel: Channel): State => State = state => {
    val now = Calendar.getInstance().getTime
    state.updateSessionFromChannel(channel, sess => {
      sess.copy(lastContact = now)
    })
  }
  
  /**
   * Compute the minimum QoS between two specified QoSs.
   *
   * @param firstQoS  the first QoS.
   * @param secondQoS the seconf QoS.
   * @return the first QoS if firstQoS < secondQoS, the second otherwise.
   */
  def min(firstQoS: QoS, secondQoS: QoS): QoS = (firstQoS, secondQoS) match {
    case (QoS(first), QoS(second)) => if (first < second) firstQoS else secondQoS
  }
  
  /**
   * Generates the sha256 of a seq of bytes, returning the corresponding sha256 string.
   *
   * @param bytes the bytes sequence.
   * @return the sha256 string.
   */
  def sha256(bytes: Seq[Byte]): String = {
    val digest = MessageDigest.getInstance("SHA-256").digest(bytes.toArray)
    String.format("%032x", new BigInteger(1, digest))
  }
}
