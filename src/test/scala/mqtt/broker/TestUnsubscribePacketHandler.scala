package mqtt.broker

import mqtt.broker.handlers.UnsubscribePacketHandler
import mqtt.broker.state.{Channel, State}
import mqtt.model.Packet

class TestUnsubscribePacketHandler extends TestUnsubscribe {
  override def UnsubscribeHandler: (State, Packet.Unsubscribe, Channel) => State =
    (state, packet, channel) => UnsubscribePacketHandler(packet, channel).handle(state)
}
