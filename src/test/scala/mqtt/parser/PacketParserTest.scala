package mqtt.parser

import mqtt.model.ErrorPacket.MalformedPacket
import mqtt.utils.Bit
import org.scalatest.{FunSuite, Matchers}
import mqtt.utils.BitImplicits._
import mqtt.model.Packet.{ApplicationMessage, Connect, Credential, Disconnect, Protocol, Puback}
import mqtt.model.QoS.QoS2

import scala.concurrent.duration._

class PacketParserTest extends FunSuite with Matchers {
  val byte: Seq[Bit] = Seq(1,1,1,0,0,0,0,0)
  val disconnect: Seq[Bit] = Seq(
    1, 1, 1, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0,
  )
  val connect: Seq[Bit] = Seq(
    0, 0, 0, 1, 0, 0, 0, 0, //id and reserved
    0, 0, 0, 1, 1, 0, 1, 0, //remaining length
    0, 0, 0, 0, 0, 0, 0, 0, //length MSB protocol name
    0, 0, 0, 0, 0, 1, 0, 0, //length LSB protocol name
    0, 1, 0, 0, 1, 1, 0, 1, //'M'
    0, 1, 0, 1, 0, 0, 0, 1, //'Q
    0, 1, 0, 1, 0, 1, 0, 0, //'T'
    0, 1, 0, 1, 0, 1, 0, 0, //'T'
    0, 0, 0, 0, 0, 1, 0, 0, //level 4
    1, 1, 1, 1, 0, 1, 1, 0, //flags
    0, 0, 0, 0, 0, 0, 0, 0, //keep alive MSB
    0, 0, 0, 0, 1, 0, 1, 0, //keep alive LSB
    0, 0, 0, 0, 0, 0, 0, 0, //length MSB client id
    0, 0, 0, 0, 0, 0, 0, 1, //length LSB client id
    0, 1, 1, 0, 0, 0, 0, 1, //'a'
    0, 0, 0, 0, 0, 0, 0, 0, //length MSB will topic
    0, 0, 0, 0, 0, 0, 0, 1, //length LSB will topic
    0, 1, 1, 0, 0, 0, 1, 0, //'b'
    0, 0, 0, 0, 0, 0, 0, 0, //length MSB will payload
    0, 0, 0, 0, 0, 0, 1, 0, //length LSB will payload
    0, 0, 0, 0, 1, 0, 1, 0, //10
    0, 0, 0, 0, 1, 0, 1, 1, //11
    0, 0, 0, 0, 0, 0, 0, 0, //length MSB username
    0, 0, 0, 0, 0, 0, 0, 1, //length LSB username
    0, 1, 1, 0, 0, 0, 1, 1, //'c'
    0, 0, 0, 0, 0, 0, 0, 0, //length MSB password
    0, 0, 0, 0, 0, 0, 0, 1, //length LSB password
    0, 0, 0, 0, 1, 1, 0, 0, //12
  )
  val puback: Seq[Bit] = Seq(0,1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0)
  
  test("An MQTT packet parser should parse a connect message") {
    PacketParser() parse connect shouldBe
      Connect(Protocol("MQTT", 4), true, 10 seconds, "a",
        Option(Credential("c", Option(Seq[Byte](12)))),
        Option(ApplicationMessage(retain = true, QoS2, "b", Seq[Byte](10, 11))))
  }
  test("An MQTT packet parser should parse a disconnect message") {
    PacketParser() parse disconnect shouldBe Disconnect
  }
  test("An MQTT packet parser should parse a puback message") {
    PacketParser() parse puback shouldBe Puback(0)
  }
  test("An MQTT packet parser should not parse an incomplete packet") {
    PacketParser() parse byte shouldBe MalformedPacket
  }
  test("An MQTT packet parser should not parse superabundant packet") {
    PacketParser() parse (disconnect ++ disconnect) shouldBe MalformedPacket
  }
}