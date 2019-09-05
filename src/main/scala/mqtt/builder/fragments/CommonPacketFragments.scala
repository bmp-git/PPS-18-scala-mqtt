package mqtt.builder.fragments

import mqtt.builder.buildContext._
import mqtt.model.Packet._
import mqtt.model.{Packet, PacketID}
import mqtt.utils.{Bit, VariableLengthInteger}
import mqtt.utils.BitImplicits._
import mqtt.builder.fragments.packetFragmentImplicits._


package object commonPacketFragments {
  //Need to be def because of equality check, to rethink if possible
  def RemainingLength: PacketFragment[Packet] = new PacketFragment[Packet] {
    override def build[R <: Packet](packet: R)(implicit context: Context[R]): Seq[Bit] = {
      VariableLengthInteger.encode(
        context.parent.fold(0)
        (parent =>
          PacketFragmentList(parent.packetFragments.dropWhile(!_.eq(this)).drop(1)).build(packet)(context).length / 8
        )).toBitsSeq
    }
  }
  
  val ControlPacketType: PacketFragment[Packet] = (p: Packet) => (p match {
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
  
  val PacketIdentifier: PacketFragment[Packet with PacketID] = (p: Packet with PacketID) => p.packetId.bits.drop(16)
  
  val Zero: StaticPacketFragment = () => Seq(0)
  
  val One: StaticPacketFragment = () => Seq(1)
  
  val Empty: StaticPacketFragment = () => Seq()
}