package mqtt.broker

class TestDisconnectBrokerManager extends TestDisconnect(
  ConnectPacketHandler = BrokerManager.handle,
  DisconnectPacketHandler = BrokerManager.handle)
