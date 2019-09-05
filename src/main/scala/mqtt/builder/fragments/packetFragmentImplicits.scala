package mqtt.builder.fragments

import mqtt.builder.buildContext.Context
import mqtt.model.{Packet, QoS}
import mqtt.utils.{Bit, MqttBytes, MqttString}
import mqtt.utils.BitImplicits._


package object packetFragmentImplicits {
  implicit def fromBooleanExtractorToPacketFragment[T <: Packet](f: T => Boolean): PacketFragment[T] = new PacketFragment[T] {
    override def build[R <: T](packet: R)(implicit context: Context[R]): Seq[Bit] = Seq(f(packet))
  }
  
  implicit def fromBitSeqExtractorToPacketFragment[T <: Packet](f: T => Seq[Bit]): PacketFragment[T] = new PacketFragment[T] {
    override def build[R <: T](packet: R)(implicit context: Context[R]): Seq[Bit] = f(packet)
  }
  
  implicit def fromOptionBitSeqExtractorToPacketFragment[T <: Packet](f: T => Option[Seq[Bit]]): PacketFragment[T] = new PacketFragment[T] {
    override def build[R <: T](packet: R)(implicit context: Context[R]): Seq[Bit] = f(packet).fold(Seq[Bit]())(b => b)
  }
  
  implicit def fromByteSeqExtractorToPacketFragment[T <: Packet](f: T => Seq[Byte]): PacketFragment[T] = new PacketFragment[T] {
    override def build[R <: T](packet: R)(implicit context: Context[R]): Seq[Bit] = MqttBytes.encode(f(packet)).toBitsSeq
  }
  
  implicit def fromOptionByteSeqExtractorToPacketFragment[T <: Packet](f: T => Option[Seq[Byte]]): PacketFragment[T] = new PacketFragment[T] {
    override def build[R <: T](packet: R)(implicit context: Context[R]): Seq[Bit] = f(packet).fold(Seq[Bit]())(b => MqttBytes.encode(b).toBitsSeq)
  }
  
  implicit def fromStringExtractorToPacketFragment[T <: Packet](f: T => String): PacketFragment[T] = new PacketFragment[T] {
    override def build[R <: T](packet: R)(implicit context: Context[R]): Seq[Bit] = MqttString.encode(f(packet)).toBitsSeq
  }
  
  implicit def fromOptionStringExtractorToPacketFragment[T <: Packet](f: T => Option[String]): PacketFragment[T] = new PacketFragment[T] {
    override def build[R <: T](packet: R)(implicit context: Context[R]): Seq[Bit] = f(packet).fold(Seq[Bit]())(s => MqttString.encode(s).toBitsSeq)
  }
}
