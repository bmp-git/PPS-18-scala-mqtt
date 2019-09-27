package mqtt.broker

import mqtt.broker.handlers.PublishPacketHandler

class TestPublishPacketHandler extends TestPublish(PublishPacketHandler = {
  (state, packet, channel) => PublishPacketHandler(packet, channel).handle(state)
})
