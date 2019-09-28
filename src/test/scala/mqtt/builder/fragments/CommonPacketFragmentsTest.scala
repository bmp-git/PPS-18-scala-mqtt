package mqtt.builder.fragments

import mqtt.builder.BuildContext._
import mqtt.builder.fragments.CommonPacketFragments._
import mqtt.builder.fragments.RichPacketFragment._
import mqtt.model.Types._
import mqtt.model.{Packet, PacketID, QoS}
import mqtt.utils.Bit
import mqtt.utils.BitImplicits._
import org.scalatest.FunSuite

import scala.concurrent.duration._

class CommonPacketFragmentsTest extends FunSuite {
  
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
    assert(empty.build() == Seq[Bit]())
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
    assert(bytesStructure.build(Seq[Byte](1, 2, 3)) == Seq[Bit](
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, //length
      0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1))
    
    assert(bytesStructure.build((0 until 1234).map(_.toByte)).getValue(0, 16) == 1234)
  }
  
  test("stringStructure should always build to the specified values and the prefixed 2 byte length") {
    assert(stringStructure.build("abc") == Seq[Bit](
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, //length
      0, 1, 1, 0, 0, 0, 0, 1, 0, 1, 1, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 0, 1, 1))
  }
  
  test("qosStructure should build always to 00 or 01 or 10") {
    assert(qosStructure.build(QoS(0)) == Seq[Bit](0, 0))
    assert(qosStructure.build(QoS(1)) == Seq[Bit](0, 1))
    assert(qosStructure.build(QoS(2)) == Seq[Bit](1, 0))
  }
  
  test("keepAliveStructure should build always the duration in seconds and 16 bit") {
    assert(keepAliveStructure.build(1 minute).getValue(0, 16) == 60)
    assert(keepAliveStructure.build(65536 seconds).getValue(0, 16) == 0)
  }
  
  val oneByte: StaticPacketFragment = () => Seq(0, 0, 0, 0, 0, 0, 0, 0)
  
  test("Remaining length should be always the remaining length og the packet") {
    assert((remainingLength :: oneByte :: oneByte).build().getValue(0, 8) == 2)
    assert((remainingLength :: ((5 * 8) zeros)).build().getValue(0, 8) == 5)
    assert((remainingLength :: oneByte :: remainingLength :: oneByte).build().getValue(16, 8) == 1)
    assert((remainingLength :: oneByte :: remainingLength).build().getValue(16, 8) == 0)
    assert((remainingLength :: (rawBytes of (0 until 128).map(_.toByte))).build().take(16) ==
      Seq[Bit](1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1))
    
  }
}
