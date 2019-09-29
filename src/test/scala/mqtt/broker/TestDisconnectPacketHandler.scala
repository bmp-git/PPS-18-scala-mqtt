package mqtt.broker

import mqtt.broker.handlers.{ConnectPacketHandler, DisconnectPacketHandler}
import mqtt.broker.state.{Channel, State}
import mqtt.model.Packet


class TestDisconnectPacketHandler extends TestDisconnect {
  override def ConnectHandler: (State, Packet.Connect, Channel) => State =
    (state, packet, channel) => ConnectPacketHandler(packet, channel).handle(state)
  
  override def DisconnectHandler: (State, Packet.Disconnect, Channel) => State =
    (state, packet, channel) => DisconnectPacketHandler(packet, channel).handle(state)
}