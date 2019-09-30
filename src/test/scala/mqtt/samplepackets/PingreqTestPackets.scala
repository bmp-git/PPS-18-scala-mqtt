package mqtt.samplepackets

import mqtt.model.Packet
import mqtt.model.Packet.Pingreq
import mqtt.utils.Bit
import mqtt.utils.BitImplicits._

object PingreqTestPackets {
  val samples: Map[Packet, Seq[Bit]] = Map(
    Pingreq() ->
      Seq(
        1, 1, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,
      ))
}
