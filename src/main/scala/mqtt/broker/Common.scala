package mqtt.broker

import mqtt.model.Packet
import mqtt.model.Packet.ApplicationMessage

object Common {
  def closeSocketWithPackets(socket: Socket, closePackets: Seq[Packet]): State => State = state => {
      publishWillMessage(socket)(state).addClosingChannel(socket, closePackets)
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
}
