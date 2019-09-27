package mqtt.broker

import mqtt.broker.handlers.{ConnectPacketHandler, DisconnectPacketHandler}


class TestDisconnectPacketHandler extends TestDisconnect(
  ConnectPacketHandler = ConnectPacketHandler.handle,
  DisconnectPacketHandler = DisconnectPacketHandler.handle)
