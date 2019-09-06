package mqtt.builder.fragments

import mqtt.builder.buildContext._
import mqtt.model.Packet
import mqtt.utils.Bit

/**
 * Defines an abstraction for a packet's fragment. It can be chained to other fragments and can be built into
 * a sequence of bits.
 *
 * @tparam P the type of the object that this fragment refers to
 */
trait PacketFragment[-P] {
  /**
   * Chain two packet fragments by merging them into one.
   *
   * @param packetFragment the fragment to be chained
   * @tparam R the type of the object that the input fragment's refers to
   * @return a new fragment representing the two
   */
  def ::[R <: P](packetFragment: PacketFragment[R]): PacketFragment[R] = packetFragment match {
    case pfp: PacketFragmentPair[P] => pfp.left :: pfp.right :: this //unpack pairs to respect (x,(x,(x,...))) structure
    case _ => PacketFragmentPair(packetFragment, this)
  }
  
  /**
   * Builds this fragment in the correspondent sequence of bits.
   *
   * @param packet  the object that contains useful information for the fragment's build
   * @param context the build context
   * @tparam R the type of the object that the input fragment's refers to
   * @return the bit sequence that this packet fragment represent with the given input
   */
  def build[R <: P](packet: R)(implicit context: Context[R]): Seq[Bit]
}

/**
 * Data structure used to chain packet fragments.
 * Example a :: b
 *
 * @param left  the left of the merged fragments (a)
 * @param right the right of the merged fragments (b)
 * @tparam P the type of the object that this fragment refers to
 */
case class PacketFragmentPair[-P](left: PacketFragment[P], right: PacketFragment[P]) extends PacketFragment[P] {
  override def build[R <: P](packet: R)(implicit context: Context[R]): Seq[Bit] =
    left.build(packet)(Context(Option(this))) ++ right.build(packet)(Context(Option(this)))
}