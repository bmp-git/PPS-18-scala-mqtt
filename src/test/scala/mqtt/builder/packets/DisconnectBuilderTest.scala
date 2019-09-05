package mqtt.builder.packets

import mqtt.builder.MqttPacketBuilder
import mqtt.model.Packet.Disconnect
import mqtt.utils.Bit
import mqtt.utils.BitImplicits._
import org.scalatest.FunSuite


class DisconnectBuilderTest extends FunSuite {
  Map[Disconnect, Seq[Bit]](
    Disconnect() ->
      Seq(
        1, 1, 1, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,
      )) foreach {
    case (packet, bits) => {
      val build = MqttPacketBuilder.build(packet)
      val buildString = build.toBinaryString
      test(s"$packet should be builded in $buildString") {
        assert(build == bits)
      }
    }
  }
}
