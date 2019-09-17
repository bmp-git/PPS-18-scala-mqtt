package mqtt.broker

import mqtt.model.Packet
import mqtt.model.Packet.{Connect, Disconnect}


object BrokerManager extends ProtocolManager {
  override def handle(state: State, packet: Packet, channel: Channel): State = {
    //TODO refactor
    //if the channel is closing the packet cannot be accepted
    state.closing.get(channel).fold({
      packet match {
        case p: Connect => ConnectPacketHandler.handle(state, p, channel)
        case p: Disconnect => DisconnectPacketHandler.handle(state, p, channel)
        case _ => println("Packet not supported"); state
      }
    })(_ => state)
  }
}
