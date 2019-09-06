package mqtt.builder.fragments

import mqtt.builder.BuildContext.Context
import mqtt.utils.Bit

/**
 * Abstraction for a packet fragment that do not need external information.
 */
trait StaticPacketFragment extends PacketFragment[Any] {
  override def build[R <: Any](packet: R)(implicit context: Context[R]): Seq[Bit] = build()
  
  /**
   * Defines the static definition of this fragment.
   *
   * @return the static sequence of bits
   */
  def build(): Seq[Bit]
  
  /**
   * Chain two StaticPacketFragment and return a new one
   * @param packetFragment the StaticPacketFragment to chain
   * @return a new StaticPacketFragment
   */
  def ::(packetFragment: StaticPacketFragment): StaticPacketFragment = () => packetFragment.build() ++ this.build()
}