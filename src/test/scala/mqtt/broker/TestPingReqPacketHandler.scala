package mqtt.broker

import mqtt.broker.handlers.PingReqPacketHandler
import mqtt.broker.state.{Channel, State}
import mqtt.model.Packet.Pingreq

class TestPingReqPacketHandler extends TestPingReq {
  override def PingReqHandler: (State, Pingreq, Channel) => State =
    (state, packet, channel) => PingReqPacketHandler(packet, channel).handle(state)
}
