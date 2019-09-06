package mqtt.builder.fragments

import mqtt.builder.buildContext.Context
import mqtt.model.Packet
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
  
  def ::(packetFragment: StaticPacketFragment): StaticPacketFragment = () => packetFragment.build() ++ this.build()
}