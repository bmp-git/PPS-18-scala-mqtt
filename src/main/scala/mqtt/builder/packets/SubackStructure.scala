package mqtt.builder.packets

import mqtt.builder.PacketStructure
import mqtt.builder.fragments.CommonPacketFragments._
import mqtt.builder.fragments.PacketFragment
import mqtt.builder.fragments.PacketFragmentImplicits._
import mqtt.builder.fragments.RichPacketFragment._
import mqtt.model.Packet.Suback
import mqtt.model.QoS

/**
 * Provide the structure of Suback packet as referred in chapter 3.9.
 */
case object SubackStructure extends PacketStructure[Suback] {
  
  //3.9.3
  private val failure = (p: Option[QoS]) => p.isEmpty
  private val qos = (p: Option[QoS]) => p.fold(zero :: zero)(qosStructure of _)
  
  override val fixedHeader: PacketFragment[Suback] = controlPacketType(9) :: (4 zeros) :: remainingLength //3.9.1
  
  override val variableHeader: PacketFragment[Suback] = packetIdentifier //3.9.2
  
  override val payload: PacketFragment[Suback] = (failure :: (5 zeros) :: qos) foreach ((p: Suback) => p.subscriptions) //3.9.3
  
}
