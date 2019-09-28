package mqtt.broker

import mqtt.broker.handlers.SubscribePacketHandler
import mqtt.broker.state.{Channel, State}
import mqtt.model.Packet

class TestSubscribePacketHandler extends TestSubscribe {
  override def SubscribeHandler: (State, Packet.Subscribe, Channel) => State =
    (state, packet, channel) => SubscribePacketHandler(packet, channel).handle(state)
}
