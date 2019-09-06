package mqtt.builder

import mqtt.model.Packet
import mqtt.utils.Bit
import mqtt.builder.fragments.CommonPacketFragments.empty
import mqtt.builder.fragments.PacketFragment

/**
 * Provides an abstraction for a MQTT 3.1.1 packet structure.
 *
 * @tparam T the type of the packet for which to specify the structure
 */
trait PacketStructure[T <: Packet] {
  def fixedHeader: PacketFragment[T]
  
  def variableHeader: PacketFragment[T] = empty
  
  def payload: PacketFragment[T] = empty
  
  def build(packet: T): Seq[Bit] = (fixedHeader :: variableHeader :: payload).build(packet)
}
