package mqtt.samplepackets

import mqtt.model.Packet
import mqtt.model.Packet.Pubrec
import mqtt.utils.Bit
import mqtt.utils.BitImplicits._

object PubrecTestPackets {
  val samples: Map[Packet, Seq[Bit]] = Map(
    Pubrec(1234) ->
      Seq(
        0, 1, 0, 1, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 1, 0, //Remaining length
        0, 0, 0, 0, 0, 1, 0, 0, //Packet id MSB
        1, 1, 0, 1, 0, 0, 1, 0, //Packet id LSB
      ))
}
