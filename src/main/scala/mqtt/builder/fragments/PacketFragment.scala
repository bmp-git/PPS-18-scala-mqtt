package mqtt.builder.fragments

import mqtt.builder.buildContext._
import mqtt.model.Packet
import mqtt.utils.Bit


trait PacketFragment[-P] {
  def ::[R <: P](packetFragment: PacketFragment[R]): PacketFragment[R] = packetFragment match {
    case pfp:PacketFragmentPair[P] => pfp.left :: pfp.right :: this //unpack pairs to respect (x,(x,(x,...))) structure
    case _ => PacketFragmentPair(packetFragment, this)
  }
  
  def build[R <: P](packet: R)(implicit context: Context[R]): Seq[Bit]
}

trait StaticPacketFragment extends PacketFragment[Packet] {
  override def build[R <: Packet](packet: R)(implicit context: Context[R]): Seq[Bit] = build()
  
  def build(): Seq[Bit]
  
  def ::(packetFragment: StaticPacketFragment): StaticPacketFragment = () => packetFragment.build() ++ this.build()
}

case class PacketFragmentPair[-P](left: PacketFragment[P], right:PacketFragment[P]) extends PacketFragment[P] {
  override def build[R <: P](packet: R)(implicit context: Context[R]): Seq[Bit] =
    left.build(packet)(Context(Option(this))) ++ right.build(packet)(Context(Option(this)))
}