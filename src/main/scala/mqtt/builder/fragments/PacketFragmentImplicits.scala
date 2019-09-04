package mqtt.builder.fragments

import mqtt.model.Packet
import mqtt.utils.Bit
import mqtt.utils.BitImplicits._

object PacketFragmentImplicits {
  implicit def fromBooleanExtractorToPacketFragment[T <: Packet](f: T => Boolean): DynamicPacketFragment[T] = new DynamicPacketFragment[T] {
    override def build[R <: T](packet: R, context: BuildContext[R]): Seq[Bit] = Seq(f(packet))
  }
  
  implicit def fromBitSeqExtractorToPacketFragment[T <: Packet](f: T => Seq[Bit]): DynamicPacketFragment[T] = new DynamicPacketFragment[T] {
    override def build[R <: T](packet: R, context: BuildContext[R]): Seq[Bit] = f(packet)
  }
}
