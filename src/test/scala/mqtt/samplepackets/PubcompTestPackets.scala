package mqtt.samplepackets

import mqtt.model.Packet
import mqtt.model.Packet.Pubcomp
import mqtt.utils.Bit
import mqtt.utils.BitImplicits._

object PubcompTestPackets {
  val samples: Map[Packet, Seq[Bit]] = Map(
    Pubcomp(1234) ->
      Seq(
        0, 1, 1, 1, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 1, 0, //Remaining length
        0, 0, 0, 0, 0, 1, 0, 0, //Packet id MSB
        1, 1, 0, 1, 0, 0, 1, 0, //Packet id LSB
      ))
}
