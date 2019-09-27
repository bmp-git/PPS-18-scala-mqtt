package mqtt.builder.fragments

import mqtt.builder.BuildContext.Context
import mqtt.builder.fragments.CommonPacketFragments._
import mqtt.builder.fragments.RichPacketFragment._
import mqtt.model.QoS
import mqtt.utils.Bit

import scala.concurrent.duration.Duration

/**
 * Provides some implicits in order to facilitate the definition of MQTT 3.1.1 packet structures.
 * Example: a packet fragment for a certain string field can be defined as: "(p: YourPacket) => p.myStringField";
 * it will produces an encoded string as specified at 1.5.3.
 */
object PacketFragmentImplicits {
  implicit def fromBooleanExtractorToPacketFragment[T](ex: T => Boolean): PacketFragment[T] = rawBit from ex
  
  implicit def fromBitSeqExtractorToPacketFragment[T](ex: T => Seq[Bit]): PacketFragment[T] = rawBits from ex
  
  implicit def fromStringExtractorToPacketFragment[T](ex: T => String): PacketFragment[T] = stringStructure from ex
  
  implicit def fromOptionStringExtractorToPacketFragment[T](ex: T => Option[String]): PacketFragment[T] = (t: T) => ex(t).fold(empty)(stringStructure of _)
  
  implicit def fromQoSExtractorToPacketFragment[T](ex: T => QoS): PacketFragment[T] = qosStructure from ex
  
  implicit def fromDurationExtractorToPacketFragment[T](ex: T => Duration): PacketFragment[T] = keepAliveStructure from ex
  
  implicit def dynamicPacketFragment[T](selector: T => PacketFragment[T]): PacketFragment[T] = new PacketFragment[T] {
    override def build[R <: T](packet: R)(implicit context: Context[R]): Seq[Bit] = selector(packet).build(packet)
  }
}
