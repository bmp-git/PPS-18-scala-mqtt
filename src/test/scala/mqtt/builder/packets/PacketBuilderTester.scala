package mqtt.builder.packets

import mqtt.builder.MqttPacketBuilder
import mqtt.model.Packet
import mqtt.samplepackets._
import mqtt.utils.Bit
import mqtt.utils.BitImplicits._
import org.scalatest.FunSuite

class PacketBuilderTester extends FunSuite {
  def assertBuild[P <: Packet](expected: Map[P, Seq[Bit]]): Unit = {
    expected foreach {
      case (packet, bits) => {
        val build = MqttPacketBuilder.build(packet)
        val buildString = build.toBinaryString.grouped(9 * 4).mkString("\n")
        test(s"$packet should be built in \n$buildString") {
          assert(build == bits)
        }
      }
    }
  }
  
  assertBuild(ConnectTestPackets samples)
  assertBuild(ConnackTestPackets samples)
  assertBuild(DisconnectTestPackets samples)
  
  assertBuild(PublishTestPackets samples)
  
  assertBuild(SubscribeTestPackets samples)
  assertBuild(SubackTestPackets samples)
  
}
