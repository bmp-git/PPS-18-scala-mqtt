package mqtt.parser

import mqtt.model.ErrorPacket.MalformedPacket
import mqtt.utils.Bit
import org.scalatest.{FunSuite, Matchers}
import mqtt.utils.BitImplicits._
import mqtt.model.Packet.Disconnect

class PacketParserTest extends FunSuite with Matchers {
  val byte1: Seq[Bit] = Seq(1,1,1,0,0,0,0,0)
  val byte2: Seq[Bit] = Seq(0,0,0,0,0,0,0,0)
  val disconnect: Seq[Bit] = byte1 ++ byte2
  
  test("An MQTT packet parser should parse a disconnect message") {
    PacketParser() parse disconnect shouldBe Disconnect
  }
  test("An MQTT packet parser should not parse an incomplete packet") {
    PacketParser() parse byte2 shouldBe MalformedPacket
  }
  test("An MQTT packet parser should not parse superabundant packet") {
    PacketParser() parse (disconnect ++ disconnect) shouldBe MalformedPacket
  }
}