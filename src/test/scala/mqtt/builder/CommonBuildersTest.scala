package mqtt.builder

import mqtt.builder.BuildContext._
import mqtt.builder.BuilderImplicits._
import mqtt.builder.CommonBuilders._
import mqtt.builder.RichBuilder._
import mqtt.model.Types._
import mqtt.model.{Packet, PacketID, QoS}
import mqtt.utils.Bit
import mqtt.utils.BitImplicits._
import org.scalatest.FunSuite

import scala.concurrent.duration._

class CommonBuildersTest extends FunSuite {
  
  case class DummyPacket(packetId: PackedID) extends Packet with PacketID
  
  test("RawBits should be an identity builder") {
    assert((rawBits build Seq[Bit](0, 1, 0, 1)) == Seq[Bit](0, 1, 0, 1))
  }
  
  test("RawBit should build to a single bit") {
    assert((rawBit build false) == Seq[Bit](0))
    assert((rawBit build true) == Seq[Bit](1))
  }
  
  test("RawBytes should build to the correspondents bits") {
    assert((rawBytes build Seq[Byte](-1, 10)) == Seq[Bit](1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 1, 0, 1, 0))
  }
  
  test("Zero should build in 0") {
    assert(zero.build() == Seq[Bit](0))
  }
  test("One should build in 1") {
    assert(one.build() == Seq[Bit](1))
  }
  test("5 zeros should build in 00000") {
    assert((5 zeros).build() == Seq[Bit](0, 0, 0, 0, 0))
  }
  test("5 ones should build in 11111") {
    assert((5 ones).build() == Seq[Bit](1, 1, 1, 1, 1))
  }
  test("Empty should build in empty-seq") {
    assert(empty.build() == Seq.empty)
  }
  test("Concat of One Zero Empty should build in 10") {
    assert((one :: zero :: empty).build() == Seq[Bit](1, 0))
  }
  
  test("PacketIdentifier should always be 2 bytes") {
    assert(packetIdentifier.build(DummyPacket(70000)).length == 16)
  }
  
  test("PacketIdentifier of 65535 should be 11111111-11111111") {
    assert(packetIdentifier.build(DummyPacket(65535)) == Seq[Bit](1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1))
  }
  
  test("ControlPacketType should always build in 4 bits") {
    (0 until 16) foreach (v => assert(controlPacketType(v).build().length == 4))
  }
  
  test("ControlPacketType(3) should build always to 0011") {
    assert(controlPacketType(3).build() == Seq[Bit](0, 0, 1, 1))
  }
  
  test("byteStructure should always build to the specified values and the prefixed 2 byte length") {
    assert(bytesBuilder.build(Seq[Byte](1, 2, 3)) == Seq[Bit](
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, //length
      0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1))
    
    assert(bytesBuilder.build((0 until 1234).map(_.toByte)).getValue(0, 16) == 1234)
  }
  
  test("stringStructure should always build to the specified values and the prefixed 2 byte length") {
    assert(stringBuilder.build("abc") == Seq[Bit](
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, //length
      0, 1, 1, 0, 0, 0, 0, 1, 0, 1, 1, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 0, 1, 1))
  }
  
  test("qosStructure should build always to 00 or 01 or 10") {
    assert(qosBuilder.build(QoS(0)) == Seq[Bit](0, 0))
    assert(qosBuilder.build(QoS(1)) == Seq[Bit](0, 1))
    assert(qosBuilder.build(QoS(2)) == Seq[Bit](1, 0))
  }
  
  test("keepAliveStructure should build always the duration in seconds and 16 bit") {
    assert(keepAliveBuilder.build(1 minute).getValue(0, 16) == 60)
    assert(keepAliveBuilder.build(65536 seconds).getValue(0, 16) == 0)
  }
  
  val oneByte: StaticBuilder = () => Seq(0, 0, 0, 0, 0, 0, 0, 0)
  
  test("Remaining length should be always the remaining length of the packet") {
    assert(remainingLength.build() == Seq[Bit](0, 0, 0, 0, 0, 0, 0, 0))
    assert((remainingLength :: oneByte :: oneByte).build().getValue(0, 8) == 2)
    assert((remainingLength :: ((5 * 8) zeros)).build().getValue(0, 8) == 5)
    assert((remainingLength :: oneByte :: remainingLength :: oneByte).build().getValue(16, 8) == 1)
    assert((remainingLength :: oneByte :: remainingLength).build().getValue(16, 8) == 0)
    assert((remainingLength :: (rawBytes of (0 until 128).map(_.toByte))).build().take(16) ==
      Seq[Bit](1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1))
  }
  
  test("OrBuilder should give Option.empty if none of the sub-builders matches") {
    val builder1: Builder[String] = (s: String) => s.length.bits
    val builder2: Builder[Int] = (p: Int) => p.bits
    val builder3: Builder[Int] = (p: Int) => p.bits
    assert(((builder1 || builder2) build 23) == 23.bits)
    assert(((builder1 || builder2) build "abc") == 3.bits)
    assert(((builder1 || builder2) buildOption 3.4).isEmpty)
    assert(((builder2 || builder3) build 23) == (23.bits ++ 23.bits))
  }
}
