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
  val remainingLength: PacketFragment[Any] = new PacketFragment[Any] {
    override def build[R <: Any](packet: R)(implicit context: Context[R]): Seq[Bit] = {
      val data = context.parent.fold(Seq[Bit]())(p => p.right match {
        case `remainingLength` => Seq[Bit]()
        case _ => p.right.build(packet)(context)
      })
      VariableLengthInteger.encode(data.length / 8).toBitsSeq
    }
  }
  
  /**
   * Builds in a 4 bits sequence representing the type of the MQTT packet.
   * Reference table: 2.1.
   */
  val controlPacketType: Int => StaticPacketFragment = value => () => value.toByte.bits.drop(4)
  
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
  
  /**
   * Enrich Int object with zeros and ones StaticPacketFragment.
   *
   * @param value the number of zeros or ones
   */
  implicit class RichInt(value: Int) {
    private def of(i: StaticPacketFragment) = Stream.fill(value)(i).fold(empty)(_ :: _)
    
    val zeros: StaticPacketFragment = of(zero)
    val ones: StaticPacketFragment = of(one)
  }
}