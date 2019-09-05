package mqtt.builder

import mqtt.model.Packet
import mqtt.utils.Bit
import mqtt.builder.fragments.CommonPacketFragments.Empty
import mqtt.builder.fragments.PacketFragment


trait PacketStructure[T <: Packet] {
  def fixedHeader: PacketFragment[T]
  
  def variableHeader: PacketFragment[T] = Empty
  
  def payload: PacketFragment[T] = Empty
  
  def build(packet: T): Seq[Bit] = (fixedHeader | variableHeader | payload).build(packet)
}
