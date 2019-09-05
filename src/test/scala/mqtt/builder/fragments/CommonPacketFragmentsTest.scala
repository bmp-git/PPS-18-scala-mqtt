package mqtt.builder.fragments

import mqtt.builder.fragments.commonPacketFragments._
import mqtt.model.Packet._
import mqtt.model.Packet.ConnectReturnCode.ConnectionAccepted
import mqtt.model.QoS.QoS1
import mqtt.model.{Packet, PacketID}
import mqtt.utils.Bit
import org.scalatest.FunSuite
import mqtt.utils.BitImplicits._
import mqtt.model.Types._
import scala.concurrent.duration._
import mqtt.builder.buildContext._


class CommonPacketFragmentsTest extends FunSuite {
  
  case class DummyPacket(packetId: PackedID) extends Packet with PacketID
  
  case object VoidPacket extends Packet
  
  test("Zero should build in 0") {
    assert(Zero.build() == Seq[Bit](0))
  }
  test("One should build in 1") {
    assert(One.build() == Seq[Bit](1))
  }
  test("Empty should build in empty-seq") {
    assert(Empty.build() == Seq[Bit]())
  }
  test("Concat of One Zero Empty should build in 10") {
    assert((One | Zero | Empty).build() == Seq[Bit](1, 0))
  }
  
  test("PacketIdentifier should always be 2 bytes") {
    assert(PacketIdentifier.build(DummyPacket(70000)).length == 16)
  }
  
  test("PacketIdentifier of 65535 should be 11111111-11111111") {
    assert(PacketIdentifier.build(DummyPacket(65535)) == Seq[Bit](1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1))
  }
  
  val OneByte: StaticPacketFragment = () => Seq(0, 0, 0, 0, 0, 0, 0, 0)
  
  test("Remaining length should be always the remaining length og the packet") {
    assert((RemainingLength | OneByte | OneByte).build(VoidPacket).getValue(0, 8) == 2)
    assert((RemainingLength | OneByte | OneByte | OneByte | OneByte | OneByte).build(VoidPacket).getValue(0, 8) == 5)
    assert((RemainingLength | OneByte | RemainingLength | OneByte).build(VoidPacket).getValue(16, 8) == 1)
    //For more need VariableLengthInteger.decode
  }
  
  
  Map[Packet, Seq[Bit]](
    Connect(Protocol("MQTT", 4), cleanSession = true, 5 seconds, "", Option.empty, Option.empty) -> Seq[Bit](0, 0, 0, 1),
    Connack(sessionPresent = false, ConnectionAccepted) -> Seq[Bit](0, 0, 1, 0),
    Publish(duplicate = false, 1234, ApplicationMessage(retain = false, QoS1, "", Seq())) -> Seq[Bit](0, 0, 1, 1),
    Puback(1234) -> Seq[Bit](0, 1, 0, 0),
    Pubrec(1234) -> Seq[Bit](0, 1, 0, 1),
    Pubrel(1234) -> Seq[Bit](0, 1, 1, 0),
    Pubcomp(1234) -> Seq[Bit](0, 1, 1, 1),
    Subscribe(1234, Seq()) -> Seq[Bit](1, 0, 0, 0),
    Suback(1234, Seq()) -> Seq[Bit](1, 0, 0, 1),
    Unsubscribe(1234, Seq()) -> Seq[Bit](1, 0, 1, 0),
    Unsuback(1234) -> Seq[Bit](1, 0, 1, 1),
    Pingreq -> Seq[Bit](1, 1, 0, 0),
    Pingresp -> Seq[Bit](1, 1, 0, 1),
    Disconnect -> Seq[Bit](1, 1, 1, 0),
  ) foreach {
    case (packet, bits) => {
      val binaryString = bits.toBinaryString
      test(s"$packet packet type should encode in $binaryString") {
        assert(ControlPacketType.build(packet) == bits)
      }
    }
  }
}
