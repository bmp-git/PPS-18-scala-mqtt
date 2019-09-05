package mqtt.builder.fragments

import mqtt.builder.buildContext._
import mqtt.model.Packet
import mqtt.utils.Bit


trait PacketFragment[-P] {
  def |[R <: P](packetFragment: PacketFragment[R]): PacketFragment[R] = PacketFragmentList(Seq(this, packetFragment))
  
  def build[R <: P](packet: R)(implicit context: Context[R]): Seq[Bit]
}

trait StaticPacketFragment extends PacketFragment[Packet] {
  override def build[R <: Packet](packet: R)(implicit context: Context[R]): Seq[Bit] = build()
  
  def build(): Seq[Bit]
  
  def |(packetFragment: StaticPacketFragment): StaticPacketFragment = () => this.build() ++ packetFragment.build()
}

case class PacketFragmentList[-P](packetFragments: Seq[PacketFragment[P]]) extends PacketFragment[P] {
  override def |[R <: P](packetFragment: PacketFragment[R]): PacketFragment[R] = PacketFragmentList(packetFragments :+ packetFragment)
  
  override def build[R <: P](packet: R)(implicit context: Context[R]): Seq[Bit] = packetFragments.flatMap(_.build(packet)(Context(Option(this))))
}


