package mqtt.broker

import mqtt.broker.handlers.PublishPacketHandler
import mqtt.broker.state.{Channel, State}
import mqtt.model.Packet

class TestPublishPacketHandler extends TestPublish {
  override def PublishHandler: (State, Packet.Publish, Channel) => State = (state, packet, channel) => PublishPacketHandler(packet, channel).handle(state)
}
