package mqtt.broker

import mqtt.broker.handlers.{ConnectPacketHandler, DisconnectPacketHandler}


class TestDisconnectPacketHandler extends TestDisconnect(
  ConnectPacketHandler = {
    (state, packet, channel) => ConnectPacketHandler(packet, channel).handle(state)
  },
  DisconnectPacketHandler = {
    (state, packet, channel) => DisconnectPacketHandler(packet, channel).handle(state)
  })
