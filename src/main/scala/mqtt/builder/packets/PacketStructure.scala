package mqtt.builder.packets

import mqtt.builder.Builder
import mqtt.model.Packet
import mqtt.utils.Bit

import scala.reflect.ClassTag

/**
 * Provides an abstraction for a MQTT 3.1.1 packet structure.
 *
 * @tparam T the type of the packet for which to specify the structure
 */
abstract class  PacketStructure[T <: Packet : ClassTag] {
  def builder: Builder[T]
  
  def build(input: Packet): Option[Seq[Bit]] = input match {
    case packet: T => Option(builder build packet)
    case _ => Option.empty
  }
}
