package mqtt.samplepackets

import mqtt.model.Packet
import mqtt.model.Packet.Unsubscribe
import mqtt.utils.Bit
import mqtt.utils.BitImplicits._

object UnsubscribeTestPackets {
  val samples: Map[Packet, Seq[Bit]] = Map(
      Unsubscribe(1234, Seq("a/b")) ->
      Seq(
        1, 0, 1, 0, 0, 0, 1, 0,
        0, 0, 0, 0, 0, 1, 1, 1, //Remaining length
        0, 0, 0, 0, 0, 1, 0, 0, //Packet id MSB
        1, 1, 0, 1, 0, 0, 1, 0, //Packet id LSB
        0, 0, 0, 0, 0, 0, 0, 0, //Topic filter name MSB
        0, 0, 0, 0, 0, 0, 1, 1, //Topic filter name LSB
        0, 1, 1, 0, 0, 0, 0, 1, //Topic filter name 'a'
        0, 0, 1, 0, 1, 1, 1, 1, //Topic filter name '/'
        0, 1, 1, 0, 0, 0, 1, 0, //Topic filter name 'b'
      ),
    Unsubscribe(1234, Seq("a/b", "cd")) ->
      Seq(
        1, 0, 1, 0, 0, 0, 1, 0,
        0, 0, 0, 0, 1, 0, 1, 1, //Remaining length
        0, 0, 0, 0, 0, 1, 0, 0, //Packet id MSB
        1, 1, 0, 1, 0, 0, 1, 0, //Packet id LSB
        0, 0, 0, 0, 0, 0, 0, 0, //Topic filter name MSB
        0, 0, 0, 0, 0, 0, 1, 1, //Topic filter name LSB
        0, 1, 1, 0, 0, 0, 0, 1, //Topic filter name 'a'
        0, 0, 1, 0, 1, 1, 1, 1, //Topic filter name '/'
        0, 1, 1, 0, 0, 0, 1, 0, //Topic filter name 'b'
        0, 0, 0, 0, 0, 0, 0, 0, //Topic filter name MSB
        0, 0, 0, 0, 0, 0, 1, 0, //Topic filter name LSB
        0, 1, 1, 0, 0, 0, 1, 1, //Topic filter name 'c'
        0, 1, 1, 0, 0, 1, 0, 0, //Topic filter name 'd'
      ))
}
