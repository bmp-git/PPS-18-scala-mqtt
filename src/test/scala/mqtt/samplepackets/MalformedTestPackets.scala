package mqtt.samplepackets

import mqtt.model.ErrorPacket.MalformedPacket
import mqtt.model.Packet
import mqtt.utils.Bit
import mqtt.utils.BitImplicits._

object MalformedTestPackets {
  val samples: Map[Packet, Seq[Bit]] = Map(
    MalformedPacket() ->
      Seq(
        1, 1, 1, 0, 0, 0, 0, 0,
      ),
    MalformedPacket() ->
      Seq(
        1, 1, 1, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,
        1, 1, 1, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,
      ))
}
