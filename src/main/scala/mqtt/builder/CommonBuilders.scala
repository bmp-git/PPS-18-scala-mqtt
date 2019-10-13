package mqtt.builder

import java.nio.charset.StandardCharsets

import mqtt.builder.BuildContext.Context
import mqtt.builder.RichBuilder._
import mqtt.model.{PacketID, QoS}
import mqtt.utils.BitImplicits._
import mqtt.utils.{Bit, VariableLengthInteger}

import scala.concurrent.duration.Duration

/**
 * Provides some basic builder to be used in the definition of a MQTT 3.1.1 packet structure.
 */
object CommonBuilders {
  
  /**
   * Identity Builder.
   */
  val rawBits: Builder[Seq[Bit]] = new Builder[Seq[Bit]] {
    override def build[R <: Seq[Bit]](data: R)(implicit context: Context[R]): Seq[Bit] = data
  }
  
  /**
   * Builds from a single boolean.
   */
  val rawBit: Builder[Boolean] = rawBits from ((b: Boolean) => Seq[Bit](b))
  
  /**
   * Builds from a sequence of bytes, without adding any header.
   */
  val rawBytes: Builder[Seq[Byte]] = rawBits from ((data: Seq[Byte]) => data.toBitsSeq)
  
  /**
   * Builds in a variable length integer representing the remaining length of the packet as specified in 2.2.3.
   * Will count only the remaining length on the right.
   * Example: "oneByte :: remainingLength :: oneByte", remainingLength will builds in 00000001
   */
  def remainingLength: Builder[Any] = new Builder[Any] {
    private val _this = this
  
    @scala.annotation.tailrec
    def findNext[P](pair: BuilderPair[P]): Option[Builder[P]] = pair match {
      case BuilderPair(`_this`, right) => Option(right)
      case BuilderPair(_, `_this`) => Option.empty
      case BuilderPair(_, right: BuilderPair[P]) => findNext(right)
      case _ => Option.empty //only when given a wrong context
    }
    
    override def build[R <: Any](value: R)(implicit context: Context[R]): Seq[Bit] = {
      val data = context.root.fold(Seq.empty[Bit])(p => findNext(p) match {
        case Some(right) => right.build(value)(context)
        case None => Seq.empty
      })
      VariableLengthInteger.encode(data.length / 8).toBitsSeq
    }
  }
  
  /**
   * Builds in a 4 bits sequence representing the type of the MQTT packet.
   * Reference table: 2.1.
   */
  val controlPacketType: Int => StaticBuilder = value => () => value.toByte.bits.drop(4)
  
  /**
   * Build in a 16 bits sequence representing the packet identifier as specified in 2.3.1.
   */
  val packetIdentifier: Builder[PacketID] = rawBits from ((p: PacketID) => p.packetId.bits.drop(16))
  
  /**
   * Builds in a sequence of one 0 bit.
   */
  val zero: StaticBuilder = rawBit of false
  
  /**
   * Builds in a sequence of one 1 bit.
   */
  val one: StaticBuilder = rawBit of true
  
  /**
   * Builds in an empty sequence of bits.
   */
  val empty: StaticBuilder = rawBits of Seq.empty
  
  /**
   * Enrich Int object with zeros and ones StaticBuilders.
   *
   * @param value the number of zeros or ones
   */
  implicit class RichInt(value: Int) {
    private def of(i: StaticBuilder) = Stream.fill(value)(i).fold(empty)(_ :: _)
    
    def zeros: StaticBuilder = of(zero)
    
    def ones: StaticBuilder = of(one)
  }
  
  private val lengthMSB = rawBits from ((s: Seq[_]) => s.length.bits.slice(16, 24))
  private val lengthLSB = rawBits from ((s: Seq[_]) => s.length.bits.slice(24, 32))
  
  /**
   * A builder representing the byte's structure of mqtt 3.1.1.
   * Builds in a sequence of bits as prefixed with a two byte length field which indicates the number of bytes used by the binary data.
   */
  val bytesBuilder: Builder[Seq[Byte]] = lengthMSB :: lengthLSB :: rawBytes
  
  /**
   * A builder representing the string's structure of mqtt 3.1.1.
   * Builds in a sequence of bits as specified in 1.5.3
   */
  val stringBuilder: Builder[String] = (lengthMSB :: lengthLSB :: rawBytes) from ((s: String) => s.getBytes(StandardCharsets.UTF_8).toSeq)
  
  /**
   * Builds in two bits indicating the specified qos.
   */
  val qosBuilder: Builder[QoS] = rawBits from ((qos: QoS) => (qos match {
    case QoS(0) => zero :: zero
    case QoS(1) => zero :: one
    case QoS(2) => one :: zero
  }).build())
  
  /**
   * A builder representing the duration's structure of mqtt 3.1.1.
   * Builds in a sequence of bits as specified in 3.1.2.10
   */
  val keepAliveBuilder: Builder[Duration] = rawBits from ((d: Duration) => d.toSeconds.toInt.bits.drop(16))
  
}
