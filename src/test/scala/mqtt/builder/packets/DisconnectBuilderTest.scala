package mqtt.builder.packets

import mqtt.model.Packet.Disconnect
import mqtt.utils.Bit
import mqtt.utils.BitImplicits._


class DisconnectBuilderTest extends PacketBuilderTester {
  assertBuild(Map[Disconnect, Seq[Bit]](
    Disconnect() ->
      Seq(
        1, 1, 1, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,
      )))
}
