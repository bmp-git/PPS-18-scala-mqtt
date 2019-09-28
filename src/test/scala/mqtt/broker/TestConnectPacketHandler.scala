package mqtt.broker

import mqtt.broker.handlers.ConnectPacketHandler
import mqtt.broker.state.{Channel, State}
import mqtt.model.Packet


class TestConnectPacketHandler extends TestConnect {
  override def ConnectHandler: (State, Packet.Connect, Channel) => State =
  (state, packet, channel) => ConnectPacketHandler(packet, channel).handle(state)
}

