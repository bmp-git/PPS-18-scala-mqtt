package mqtt.builder.packets

import mqtt.builder.PacketStructure
import mqtt.builder.fragments.CommonPacketFragments._
import mqtt.builder.fragments.PacketFragment
import mqtt.builder.fragments.RichPacketFragment._
import mqtt.model.Packet.Subscribe
import mqtt.model.QoS
import mqtt.model.Types.TopicFilter

/**
 * Provide the structure of Subscribe packet as referred in chapter 3.8.
 */
case object SubscribeStructure extends PacketStructure[Subscribe] {
  
  //3.8.3
  private val topicQoS: PacketFragment[(String, QoS)] = qosStructure from {
    case (_, qos) => qos
  }
  private val topicFilter: PacketFragment[(String, QoS)] = stringStructure from {
    case (filter, _) => filter
  }
  
  
  override val fixedHeader: PacketFragment[Subscribe] = controlPacketType(8) :: zero :: zero :: one :: zero :: remainingLength //3.8.1
  
  override val variableHeader: PacketFragment[Subscribe] = packetIdentifier //3.8.2
  
  override val payload: PacketFragment[Subscribe] = (topicFilter :: (6 zeros) :: topicQoS) foreach ((p: Subscribe) => p.topics) //3.8.3
}
