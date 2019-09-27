package mqtt.builder.packets

import mqtt.model.Packet.Suback
import mqtt.model.QoS.{QoS0, QoS1, QoS2}
import mqtt.utils.Bit
import mqtt.utils.BitImplicits._


class SubackBuilderTest extends PacketBuilderTester {
  assertBuild(Map[Suback, Seq[Bit]](
    Suback(1234, Seq(Option(QoS0), Option(QoS1), Option(QoS2), Option.empty)) ->
      Seq(
        1, 0, 0, 1, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 1, 1, 0, //Remaining length
        0, 0, 0, 0, 0, 1, 0, 0, //Packet id MSB
        1, 1, 0, 1, 0, 0, 1, 0, //Packet id LSB
        0, 0, 0, 0, 0, 0, 0, 0, //QoS0
        0, 0, 0, 0, 0, 0, 0, 1, //QoS1
        0, 0, 0, 0, 0, 0, 1, 0, //QoS2
        1, 0, 0, 0, 0, 0, 0, 0, //None
      )))
}
