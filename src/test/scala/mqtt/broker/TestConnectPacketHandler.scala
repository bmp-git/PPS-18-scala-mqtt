package mqtt.broker

import mqtt.broker.handlers.ConnectPacketHandler


class TestConnectPacketHandler extends TestConnect(ConnectPacketHandler = ConnectPacketHandler.handle)

