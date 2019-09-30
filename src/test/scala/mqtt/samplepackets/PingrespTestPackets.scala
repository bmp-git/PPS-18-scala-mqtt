package mqtt.samplepackets

import mqtt.model.Packet
import mqtt.model.Packet.Pingresp
import mqtt.utils.Bit
import mqtt.utils.BitImplicits._

object PingrespTestPackets {
  val samples: Map[Packet, Seq[Bit]] = Map(
    Pingresp() ->
      Seq(
        1, 1, 0, 1, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, //Remaining length
      ))
}
