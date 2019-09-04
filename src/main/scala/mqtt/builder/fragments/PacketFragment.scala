package mqtt.builder.fragments

import mqtt.model.Packet
import mqtt.utils.Bit

case class BuildContext[-P](parent: Option[PacketFragmentList[P]])


trait PacketFragment[-P] {
  def |[R <: P](packetFragment: PacketFragment[R]): PacketFragment[R] = PacketFragmentList(Seq(this, packetFragment))
  
  def build[R <: P](packet: R, context: BuildContext[R] = BuildContext[R](Option.empty)): Seq[Bit]
}

trait StaticPacketFragment extends PacketFragment[Packet] {
  override def build[R <: Packet](packet: R, context: BuildContext[R]): Seq[Bit] = build()
  
  def build(): Seq[Bit]
}

trait DynamicPacketFragment[-P] extends PacketFragment[P] {
  def build[R <: P](packet: R, context: BuildContext[R]): Seq[Bit]
}

case class PacketFragmentList[-P](packetFragments: Seq[PacketFragment[P]]) extends DynamicPacketFragment[P] {
  override def |[R <: P](packetFragment: PacketFragment[R]): PacketFragment[R] = PacketFragmentList(packetFragments :+ packetFragment)
  
  override def build[R <: P](packet: R, context: BuildContext[R]): Seq[Bit] = packetFragments.flatMap {
    case dpf: DynamicPacketFragment[P] => dpf.build(packet, BuildContext(Option(this)))
    case spf: StaticPacketFragment => spf.build()
  }
}


