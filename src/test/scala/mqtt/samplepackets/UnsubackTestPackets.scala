package mqtt.samplepackets

import mqtt.model.Packet.Unsuback
import mqtt.model.{Packet, QoS}
import mqtt.utils.Bit
import mqtt.utils.BitImplicits._

object UnsubackTestPackets {
  val samples: Map[Packet, Seq[Bit]] = Map(
    Unsuback(1234) ->
      Seq(
        1, 0, 1, 1, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 1, 0, //Remaining length
        0, 0, 0, 0, 0, 1, 0, 0, //Packet id MSB
        1, 1, 0, 1, 0, 0, 1, 0, //Packet id LSB
      ))
}
