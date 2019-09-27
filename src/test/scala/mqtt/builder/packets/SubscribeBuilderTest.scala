package mqtt.builder.packets

import mqtt.model.Packet.Subscribe
import mqtt.model.QoS
import mqtt.model.QoS.{QoS0, QoS2}
import mqtt.model.Types.TopicFilter
import mqtt.utils.Bit
import mqtt.utils.BitImplicits._


class SubscribeBuilderTest extends PacketBuilderTester {
  assertBuild(Map[Subscribe, Seq[Bit]](
    Subscribe(1234, Seq()) ->
      Seq(
        1, 0, 0, 0, 0, 0, 1, 0,
        0, 0, 0, 0, 0, 0, 1, 0, //Remaining length
        0, 0, 0, 0, 0, 1, 0, 0, //Packet id MSB
        1, 1, 0, 1, 0, 0, 1, 0, //Packet id LSB
      ),
    Subscribe(1234, Seq(("a/b", QoS2))) ->
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
      )))
}
