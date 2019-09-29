package mqtt.buildparse

import mqtt.builder.MqttPacketBuilder
import mqtt.model.Packet
import mqtt.parser.MqttPacketParser
import mqtt.samplepackets._
import mqtt.utils.Bit
import mqtt.utils.BitImplicits._
import org.scalatest.FunSuite

class BuilderParserIntegrationTest extends FunSuite {
  def assertBuildParse[P <: Packet](expected: Map[P, Seq[Bit]]): Unit = {
    expected foreach {
      case (packet, bits) => {
        val b1 = MqttPacketBuilder.build(packet)
        val p1 = MqttPacketParser.parse(b1)
        val b2 = MqttPacketBuilder.build(p1)
        
        val buildString = bits.toBinaryString.grouped(9 * 4).mkString("\n")
        test(s"$packet should be built/parsed with \n$buildString") {
          assert(bits == b1 && bits == b2)
          assert(packet == p1)
        }
      }
    }
  }
  
  assertBuildParse(ConnectTestPackets samples)
  assertBuildParse(ConnackTestPackets samples)
  assertBuildParse(DisconnectTestPackets samples)
  
  assertBuildParse(PublishTestPackets samples)
  
  assertBuildParse(SubscribeTestPackets samples)
  assertBuildParse(SubackTestPackets samples)
  
}
