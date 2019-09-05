package mqtt.broker

import mqtt.model.Packet
import mqtt.model.Packet.ApplicationMessage

object Common {
  def closeSocketWithPackets(socket: Socket, closePackets: Seq[Packet]): State => State = state => {
    (publishWillMessage(socket) andThen closeSocketNoWillPublish(socket, closePackets)) (state)
  }
  
  def closeSocketNoWillPublish(socket: Socket, closePackets: Seq[Packet]): State => State = state => {
    updateSession(socket)(state).addClosingChannel(socket.setWillMessage(Option.empty), closePackets)
  }
  
  def closeSocket(socket: Socket): State => State = state => {
    closeSocketWithPackets(socket, Seq())(state)
  }
  
  def publishMessage(message: ApplicationMessage): State => State = state => {
    //TODO
    println("Message published ".concat(message.toString))
    state
  }
  
  def publishWillMessage(socket: Socket): State => State = state => {
    socket.willMessage.fold[State](state)(publishMessage(_)(state))
  }
  
  def updateSession(socket: Socket): State => State = state => {
    state.sessionFromSocket(socket).fold(state) { case (id, sess) => {
      if (sess.persistent) {
        val new_sess = sess.copy(socket = Option.empty)
        state.updateUserSession(id, _ => new_sess)
      } else {
        state.deleteUserSession(id)
      }
    }
    }
  }
  
}
