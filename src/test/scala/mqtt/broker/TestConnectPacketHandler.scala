package mqtt.broker

import mqtt.broker.handlers.ConnectPacketHandler


class TestConnectPacketHandler extends TestConnect(ConnectPacketHandler = {
  (state, packet, channel) => ConnectPacketHandler(packet, channel).handle(state)
})

