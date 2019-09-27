package mqtt.samplepackets

import mqtt.model.Packet.Subscribe
import mqtt.model.{Packet, QoS}
import mqtt.utils.Bit
import mqtt.utils.BitImplicits._

object SubscribeTestPackets {
  val samples: Map[Packet, Seq[Bit]] = Map(
    Subscribe(1234, Seq(("a/b", QoS(2)))) ->
      Seq(
        1, 0, 0, 0, 0, 0, 1, 0,
        0, 0, 0, 0, 1, 0, 0, 0, //Remaining length
        0, 0, 0, 0, 0, 1, 0, 0, //Packet id MSB
        1, 1, 0, 1, 0, 0, 1, 0, //Packet id LSB
        0, 0, 0, 0, 0, 0, 0, 0, //Topic filter name MSB
        0, 0, 0, 0, 0, 0, 1, 1, //Topic filter name LSB
        0, 1, 1, 0, 0, 0, 0, 1, //Topic filter name 'a'
        0, 0, 1, 0, 1, 1, 1, 1, //Topic filter name '/'
        0, 1, 1, 0, 0, 0, 1, 0, //Topic filter name 'b'
        0, 0, 0, 0, 0, 0, 1, 0, //QoS2
      ),
    Subscribe(1234, Seq(("a/b", QoS(2)), ("cd", QoS(0)))) ->
      Seq(
        1, 0, 0, 0, 0, 0, 1, 0,
        0, 0, 0, 0, 1, 1, 0, 1, //Remaining length
        0, 0, 0, 0, 0, 1, 0, 0, //Packet id MSB
        1, 1, 0, 1, 0, 0, 1, 0, //Packet id LSB
        0, 0, 0, 0, 0, 0, 0, 0, //Topic filter name MSB
        0, 0, 0, 0, 0, 0, 1, 1, //Topic filter name LSB
        0, 1, 1, 0, 0, 0, 0, 1, //Topic filter name 'a'
        0, 0, 1, 0, 1, 1, 1, 1, //Topic filter name '/'
        0, 1, 1, 0, 0, 0, 1, 0, //Topic filter name 'b'
        0, 0, 0, 0, 0, 0, 1, 0, //QoS2
        0, 0, 0, 0, 0, 0, 0, 0, //Topic filter name MSB
        0, 0, 0, 0, 0, 0, 1, 0, //Topic filter name LSB
        0, 1, 1, 0, 0, 0, 1, 1, //Topic filter name 'c'
        0, 1, 1, 0, 0, 1, 0, 0, //Topic filter name 'd'
        0, 0, 0, 0, 0, 0, 0, 0, //QoS2
      ))
}
