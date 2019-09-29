package mqtt.samplepackets

import mqtt.model.Packet
import mqtt.model.Packet.Disconnect
import mqtt.utils.Bit
import mqtt.utils.BitImplicits._

object DisconnectTestPackets {
  val samples: Map[Packet, Seq[Bit]] = Map(
  Disconnect() ->
    Seq(
      1, 1, 1, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 0,
    ))
}
