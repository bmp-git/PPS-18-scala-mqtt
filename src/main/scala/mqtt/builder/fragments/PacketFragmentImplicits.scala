package mqtt.builder.fragments

import mqtt.builder.BuildContext.Context
import mqtt.model.Packet
import mqtt.utils.Bit
import mqtt.utils.BitImplicits._


object PacketFragmentImplicits {
  implicit def fromBooleanExtractorToPacketFragment[T <: Packet](f: T => Boolean): PacketFragment[T] = new PacketFragment[T] {
    override def build[R <: T](packet: R)(implicit context: Context[R]): Seq[Bit] = Seq(f(packet))
  }
  
  implicit def fromBitSeqExtractorToPacketFragment[T <: Packet](f: T => Seq[Bit]): PacketFragment[T] = new PacketFragment[T] {
    override def build[R <: T](packet: R)(implicit context: Context[R]): Seq[Bit] = f(packet)
  }
}
