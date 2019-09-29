package mqtt.builder.fragments

import java.nio.charset.StandardCharsets

import mqtt.builder.BuildContext._
import mqtt.builder.fragments.RichPacketFragment._
import mqtt.model.{PacketID, QoS}
import mqtt.utils.BitImplicits._
import mqtt.utils.{Bit, VariableLengthInteger}

import scala.concurrent.duration.Duration

/**
 * Provides some basic packet fragments to be used in the definition of a MQTT 3.1.1 packet structure.
 */
object CommonPacketFragments {
  
  /**
   * Identity PacketFragment.
   */
  val rawBits: PacketFragment[Seq[Bit]] = new PacketFragment[Seq[Bit]] {
    override def build[R <: Seq[Bit]](packet: R)(implicit context: Context[R]): Seq[Bit] = packet
  }
  
  /**
   * A PacketFragment that builds from a single boolean.
   */
  val rawBit: PacketFragment[Boolean] = rawBits from ((b: Boolean) => Seq[Bit](b))
  
  /**
   * A PacketFragment that builds from a sequence of bytes, without adding any header.
   */
  val rawBytes: PacketFragment[Seq[Byte]] = rawBits from ((data: Seq[Byte]) => data.toBitsSeq)
  
  /**
   * Builds in a variable length integer representing the remaining length of the packet as specified in 2.2.3.
   * Will count only the remaining length on the right.
   * Example: "oneByte :: remainingLength :: oneByte", remainingLength will builds in 00000001
   */
  val remainingLength: PacketFragment[Any] = new PacketFragment[Any] {
    override def build[R <: Any](packet: R)(implicit context: Context[R]): Seq[Bit] = {
      val data = context.parent.fold(Seq.empty[Bit])(p => p.right match {
        case `remainingLength` => Seq.empty
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
  val packetIdentifier: PacketFragment[PacketID] = rawBits from ((p: PacketID) => p.packetId.bits.drop(16))
  
  /**
   * Builds in a sequence of one 0 bit.
   */
  val zero: StaticPacketFragment = rawBit of false
  
  /**
   * Builds in a sequence of one 1 bit.
   */
  val one: StaticPacketFragment = rawBit of true
  
  /**
   * Builds in an empty sequence of bits.
   */
  val empty: StaticPacketFragment = rawBits of Seq.empty
  
  /**
   * Enrich Int object with zeros and ones StaticPacketFragment.
   *
   * @param value the number of zeros or ones
   */
  implicit class RichInt(value: Int) {
    private def of(i: StaticPacketFragment) = Stream.fill(value)(i).fold(empty)(_ :: _)
  
    def zeros: StaticPacketFragment = of(zero)
  
    def ones: StaticPacketFragment = of(one)
  }
  
  private val lengthMSB = rawBits from ((s: Seq[_]) => s.length.bits.slice(16, 24))
  private val lengthLSB = rawBits from ((s: Seq[_]) => s.length.bits.slice(24, 32))
  
  /**
   * A packet fragment representing the byte's structure of mqtt 3.1.1.
   * Builds in a sequence of bits as prefixed with a two byte length field which indicates the number of bytes used by the binary data.
   */
  val bytesStructure: PacketFragment[Seq[Byte]] = lengthMSB :: lengthLSB :: rawBytes
  
  /**
   * A packet fragment representing the string's structure of mqtt 3.1.1.
   * Builds in a sequence of bits as specified in 1.5.3
   */
  val stringStructure: PacketFragment[String] = (lengthMSB :: lengthLSB :: rawBytes) from ((s: String) => s.getBytes(StandardCharsets.UTF_8).toSeq)
  
  /**
   * Builds in two bits indicating the specified qos.
   */
  val qosStructure: PacketFragment[QoS] = rawBits from ((qos: QoS) => (qos match {
    case QoS(0) => zero :: zero
    case QoS(1) => zero :: one
    case QoS(2) => one :: zero
  }).build())
  
  /**
   * A packet fragment representing the duration's structure of mqtt 3.1.1.
   * Builds in a sequence of bits as specified in 3.1.2.10
   */
  val keepAliveStructure: PacketFragment[Duration] = rawBits from ((d: Duration) => d.toSeconds.toInt.bits.drop(16))

}