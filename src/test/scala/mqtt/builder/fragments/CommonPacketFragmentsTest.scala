package mqtt.builder.fragments

import mqtt.builder.fragments.CommonPacketFragments._
import mqtt.model.Packet._
import mqtt.model.Packet.ConnectReturnCode.ConnectionAccepted
import mqtt.model.QoS.QoS1
import mqtt.model.{Packet, PacketID}
import mqtt.utils.Bit
import org.scalatest.FunSuite
import mqtt.utils.BitImplicits._
import mqtt.model.Types._
import scala.concurrent.duration._
import mqtt.builder.BuildContext._


class CommonPacketFragmentsTest extends FunSuite {
  
  case class DummyPacket(packetId: PackedID) extends Packet with PacketID
  
  case object VoidPacket extends Packet
  
  test("Zero should build in 0") {
    assert(zero.build() == Seq[Bit](0))
  }
  test("One should build in 1") {
    assert(one.build() == Seq[Bit](1))
  }
  test("Empty should build in empty-seq") {
    assert(empty.build() == Seq[Bit]())
  }
  test("Concat of One Zero Empty should build in 10") {
    val asd = (one :: zero :: empty)
    assert((one :: zero :: empty).build() == Seq[Bit](1, 0))
  }
  
  test("PacketIdentifier should always be 2 bytes") {
    assert(packetIdentifier.build(DummyPacket(70000)).length == 16)
  }
  
  test("PacketIdentifier of 65535 should be 11111111-11111111") {
    assert(packetIdentifier.build(DummyPacket(65535)) == Seq[Bit](1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1))
  }
  
  val OneByte: StaticPacketFragment = () => Seq(0, 0, 0, 0, 0, 0, 0, 0)
  
  test("Remaining length should be always the remaining length og the packet") {
    assert((remainingLength :: OneByte :: OneByte).build(VoidPacket).getValue(0, 8) == 2)
    assert((remainingLength :: OneByte :: OneByte :: OneByte :: OneByte :: OneByte).build(VoidPacket).getValue(0, 8) == 5)
    assert((remainingLength :: OneByte :: remainingLength :: OneByte).build(VoidPacket).getValue(16, 8) == 1)
    assert((remainingLength :: OneByte :: remainingLength).build(()).getValue(16, 8) == 0)
    //For more need VariableLengthInteger.decode
  }
}
