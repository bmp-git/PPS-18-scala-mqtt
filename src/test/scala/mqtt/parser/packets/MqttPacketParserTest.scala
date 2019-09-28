package mqtt.parser.packets

import mqtt.parser.MqttPacketParser
import mqtt.samplepackets._
import mqtt.utils.Bit
import mqtt.utils.BitImplicits._
import org.scalatest.{FunSuite, Matchers}

class MqttPacketParserTest extends FunSuite with Matchers {
  
  def assertParse[Packet](expected: Map[Packet, Seq[Bit]]): Unit = {
    expected foreach {
      case (packet, bits) => {
        val parsed = MqttPacketParser parse bits
        val bitsString = bits.toBinaryString.grouped(9 * 4).mkString("\n")
        test(s"$bitsString\n SHOULD be parsed in \n$packet") {
          parsed shouldBe packet
        }
      }
    }
  }
  
  assertParse(ConnectTestPackets samples)
  assertParse(ConnackTestPackets samples)
  assertParse(DisconnectTestPackets samples)
  
  assertParse(PublishTestPackets samples)
  assertParse(PubackTestPackets samples)
  assertParse(PubrecTestPackets samples)
  assertParse(PubrelTestPackets samples)
  assertParse(PubcompTestPackets samples)
  
  assertParse(SubscribeTestPackets samples)
  assertParse(SubackTestPackets samples)
  
  assertParse(UnsubscribeTestPackets samples)
  assertParse(UnsubackTestPackets samples)
  
  assertParse(MalformedTestPackets samples)
  
}