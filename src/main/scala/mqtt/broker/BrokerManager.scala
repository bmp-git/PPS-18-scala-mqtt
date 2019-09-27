package mqtt.broker

import mqtt.broker.Common.closeChannel
import mqtt.broker.handlers.{ConnectPacketHandler, DisconnectPacketHandler, PublishPacketHandler}
import mqtt.broker.state.{Channel, State}
import mqtt.model.ErrorPacket.{ChannelClosed, MalformedPacket}
import mqtt.model.Packet
import mqtt.model.Packet.{Connect, Disconnect, Publish}


object BrokerManager extends ProtocolManager {
  override def handle(state: State, packet: Packet, channel: Channel): State = {
    //TODO refactor
    //if the channel is closing the packet cannot be accepted
    state.closing.get(channel).fold({
      packet match {
        case p: Connect => ConnectPacketHandler(p, channel).handle(state)
        case p: Disconnect => DisconnectPacketHandler(p, channel).handle(state)
        case p: Publish => PublishPacketHandler(p, channel).handle(state)
        case _: MalformedPacket => println("Received malformed packet from ".concat(channel.toString)); closeChannel(channel)(state)
        case _: ChannelClosed => closeChannel(channel)(state)
        case _ => println("Packet not supported"); state
      }
    })(_ => state)
  }
  
  override def tick(state: State): State = state //TODO
}
