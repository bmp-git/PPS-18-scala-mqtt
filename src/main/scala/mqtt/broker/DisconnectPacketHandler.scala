package mqtt.broker

import mqtt.broker.Common.closeSocketNoWillPublish
import mqtt.model.Packet.Disconnect

object DisconnectPacketHandler extends PacketHandler[Disconnect] {
  override def handle(state: State, packet: Disconnect, socket: Socket): State = {
    closeSocketNoWillPublish(socket, Seq())(state)
  }
}
