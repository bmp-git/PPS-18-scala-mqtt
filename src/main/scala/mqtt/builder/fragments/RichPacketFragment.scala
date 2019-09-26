package mqtt.builder.fragments

import mqtt.builder.BuildContext.Context
import mqtt.utils.Bit

/**
 * Include an explicit extension for packet fragments.
 */
object RichPacketFragment {
  
  /**
   * Extends the PacketFragment trait.
   *
   * @param packetFragment the trait instance to extend
   * @tparam T the type of the fragment
   */
  implicit class RichPacketFragmentExtension[T](packetFragment: PacketFragment[T]) {
    /**
     * Transforms this packet fragment from type T to P.
     * In the building process this new fragment will require a P value.
     * Example: (stringFragment from ((v: MyStructure) => v.myString)) build (myStructure)
     *
     * @param ex the extractor, selects a T from a P
     * @tparam P the new type of the PacketFragment
     * @return the new packet fragment
     */
    def from[P](ex: P => T): PacketFragment[P] = new PacketFragment[P] {
      override def build[R <: P](packet: R)(implicit context: Context[R]): Seq[Bit] = packetFragment.build(ex(packet))
    }
    
    /**
     * Transforms this packet fragment from type T to P.
     * In the building process this new fragment will require a P value and
     * will builds foreach element flatMapping the results.
     * Example: (stringFragment foreach ((v: MyStructure) = v.myStrings) build (myStructure)
     *
     * @param ex the extractor, select a T collection from a P
     * @tparam P the new type of the PacketFragment
     * @return the new packet fragment
     */
    def foreach[P](ex: P => Seq[T]): PacketFragment[P] = new PacketFragment[P] {
      override def build[R <: P](packet: R)(implicit context: Context[R]): Seq[Bit] =
        ex(packet).flatMap(v => packetFragment.build(v))
    }
    
    /**
     * Builds this fragment with the given data and save the result in order
     * to build a new StaticPacketFragment.
     *
     * @param data the build input
     * @tparam P the build input type
     * @return a new StaticPacketFragment representing the result of the build
     */
    def of[P <: T](data: P): StaticPacketFragment = () => packetFragment.build(data)
  }
  
}
