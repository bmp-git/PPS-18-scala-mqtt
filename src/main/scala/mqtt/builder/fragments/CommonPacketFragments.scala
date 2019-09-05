package mqtt.builder.fragments

import mqtt.builder.fragments.CommonPacketFragments.RemainingLength
import mqtt.model.Packet._
import mqtt.model.{Packet, PacketID}
import mqtt.utils.{Bit, VariableLengthInteger}
import mqtt.utils.BitImplicits._
import mqtt.builder.fragments.PacketFragmentImplicits._


object CommonPacketFragments {
  //Need to be def because of equality check, to rethink if possible
  def RemainingLength: DynamicPacketFragment[Packet] = new DynamicPacketFragment[Packet] {
    override def build[R <: Packet](packet: R, context: BuildContext[R]): Seq[Bit] = {
      VariableLengthInteger.encode(
        context.parent.fold(0)
        (parent =>
          PacketFragmentList(parent.packetFragments.dropWhile(!_.eq(this)).drop(1)).build(packet, context).length / 8
        )).toBitsSeq
    }
  }
  
  val ControlPacketType: DynamicPacketFragment[Packet] = (p: Packet) => (p match {
    case _: Connect => 1
    case _: Connack => 2
    case _: Publish => 3
    case _: Puback => 4
    case _: Pubrec => 5
    case _: Pubrel => 6
    case _: Pubcomp => 7
    case _: Subscribe => 8
    case _: Suback => 9
    case _: Unsubscribe => 10
    case _: Unsuback => 11
    case Pingreq => 12
    case Pingresp => 13
    case Disconnect => 14
  }).toByte.bits.drop(4)
  
  val PacketIdentifier: DynamicPacketFragment[Packet with PacketID] = (p: Packet with PacketID) => p.packetId.bits.drop(16)
  
  val Zero: StaticPacketFragment = () => Seq(0)
  
  val One: StaticPacketFragment = () => Seq(1)
  
  val Empty: StaticPacketFragment = () => Seq()
}

object Asd extends App {
  import CommonPacketFragments._
  val OneByte: StaticPacketFragment = () => Seq(0, 0, 0, 0, 0, 0, 0, 0)
  case object VoidPacket extends Packet
  println((RemainingLength | OneByte | RemainingLength | OneByte).build(VoidPacket))
}