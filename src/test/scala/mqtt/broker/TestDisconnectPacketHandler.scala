package mqtt.broker


class TestDisconnectPacketHandler extends TestDisconnect(
  ConnectPacketHandler = ConnectPacketHandler.handle,
  DisconnectPacketHandler = DisconnectPacketHandler.handle)
