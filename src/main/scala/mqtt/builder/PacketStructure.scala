package mqtt.builder

import mqtt.model.Packet
import mqtt.utils.Bit
import mqtt.builder.fragments.commonPacketFragments.empty
import mqtt.builder.fragments.PacketFragment


trait PacketStructure[T <: Packet] {
  def fixedHeader: PacketFragment[T]
  
  def variableHeader: PacketFragment[T] = empty
  
  def payload: PacketFragment[T] = empty
  
  def build(packet: T): Seq[Bit] = (fixedHeader | variableHeader | payload).build(packet)
}
