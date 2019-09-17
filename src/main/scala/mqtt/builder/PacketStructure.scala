package mqtt.builder

import mqtt.model.Packet
import mqtt.utils.Bit
import mqtt.builder.fragments.CommonPacketFragments.empty
import mqtt.builder.fragments.PacketFragment

import scala.reflect.ClassTag

/**
 * Provides an abstraction for a MQTT 3.1.1 packet structure.
 *
 * @tparam T the type of the packet for which to specify the structure
 */
abstract class  PacketStructure[T <: Packet : ClassTag] {
  def fixedHeader: PacketFragment[T]
  
  def variableHeader: PacketFragment[T] = empty
  
  def payload: PacketFragment[T] = empty
  
  def fragment: PacketFragment[T] = fixedHeader :: variableHeader :: payload
  
  def build(input: Packet): Option[Seq[Bit]] = input match {
    case packet: T => Option(fragment build packet)
    case _ => Option.empty
  }
}
