package mqtt.builder.fragments

import mqtt.model.Packet._
import mqtt.model.{Packet, PacketID}
import mqtt.utils.{Bit, VariableLengthInteger}
import mqtt.utils.BitImplicits._
import mqtt.builder.fragments.PacketFragmentImplicits._


object CommonPacketFragments {
  val RemainingLength: DynamicPacketFragment[Packet] = new DynamicPacketFragment[Packet] {
    override def build[R <: Packet](packet: R, context: BuildContext[R]): Seq[Bit] = {
      if (context.parent.isDefined) {
        //Warning: extremely inefficient
        val length = (PacketFragmentList(context.parent.get.packetFragments.dropWhile(!_.eq(this)).drop(1)).build(packet).length / 8)
        VariableLengthInteger.encode(length).toBitsSeq
      } else {
        0.toByte.bits
      }
    }
  }
  
  val ControlPacketType: DynamicPacketFragment[Packet] = (p: Packet) => (p match {
    case _:Connect => 1
    case _:Connack => 2
    case _:Publish => 3
    case _:Puback => 4
    case _:Pubrec => 5
    case _:Pubrel => 6
    case _:Pubcomp => 7
    case _:Subscribe => 8
    case _:Suback => 9
    case _:Unsubscribe => 10
    case _:Unsuback => 11
    case Pingreq => 12
    case Pingresp => 13
    case Disconnect => 14
  }).toByte.bits.drop(4)
  
  val PacketIdentifier: DynamicPacketFragment[Packet with PacketID] = (p: Packet with PacketID) => p.packetId.bits.drop(16)
  
  def Zero: StaticPacketFragment = () => Seq(0)
  
  def One: StaticPacketFragment = () => Seq(1)
  
  def Empty: StaticPacketFragment = () => Seq()
}
