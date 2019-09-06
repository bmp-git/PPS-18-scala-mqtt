package mqtt.builder.fragments

import mqtt.builder.BuildContext._
import mqtt.model.Packet._
import mqtt.model.{Packet, PacketID}
import mqtt.utils.{Bit, VariableLengthInteger}
import mqtt.utils.BitImplicits._
import mqtt.builder.fragments.PacketFragmentImplicits._

/**
 * Provides some basic packet fragments to be used in the definition of a MQTT 3.1.1 packet structure.
 */
object CommonPacketFragments {
  
  /**
   * Builds in a variable length integer representing the remaining length of the packet as specified in 2.2.3.
   * Will count only the remaining length on the right.
   * Example: "oneByte :: remainingLength :: oneByte", remainingLength will builds in 00000001
   */
  val remainingLength: PacketFragment[Packet] = new PacketFragment[Packet] {
    override def build[R <: Packet](packet: R)(implicit context: Context[R]): Seq[Bit] = {
      val data = context.parent.fold(Seq[Bit]())(_.right.build(packet)(context))
      VariableLengthInteger.encode(data.length / 8).toBitsSeq
    }
  }
  
  /**
   * Builds in a 4 bits sequence representing the type of the MQTT packet.
   * Reference table: 2.1.
   */
  val controlPacketType: PacketFragment[Packet] = (p: Packet) => (p match {
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
    case _: Pingreq => 12
    case _: Pingresp => 13
    case _: Disconnect => 14
  }).toByte.bits.drop(4)
  
  /**
   * Build in a 16 bits sequence representing the packet identifier as specified in 2.3.1.
   */
  val packetIdentifier: PacketFragment[Packet with PacketID] = (p: Packet with PacketID) => p.packetId.bits.drop(16)
  
  /**
   * Builds in a sequence of one 0 bit.
   */
  val zero: StaticPacketFragment = () => Seq(0)
  
  /**
   * Builds in a sequence of one 1 bit.
   */
  val one: StaticPacketFragment = () => Seq(1)
  
  /**
   * Builds in an empty sequence of bits.
   */
  val empty: StaticPacketFragment = () => Seq()
}