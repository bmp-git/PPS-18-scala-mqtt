package mqtt.broker

import mqtt.model.Packet
import mqtt.model.Packet.{Connect, Disconnect}


class BrokerManager extends ProtocolManager {
  override def handle(state: State, packet: Packet, socket: Socket): State = {
    //TODO refactor
    //if the socket is closing the packet cannot be accepted
    state.closing.get(socket).fold({
      packet match {
        case p: Connect => ConnectPacketHandler.handle(state, p, socket)
        case p: Disconnect => DisconnectPacketHandler.handle(state, p, socket)
        case _ => println("Packet not supported"); state
      }
    })(_ => state)
  }
}
