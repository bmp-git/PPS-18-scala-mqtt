package mqtt.builder.packets

import mqtt.builder.PacketStructure
import mqtt.builder.fragments.CommonPacketFragments._
import mqtt.builder.fragments.PacketFragment
import mqtt.builder.fragments.PacketFragmentImplicits._
import mqtt.builder.fragments.RichPacketFragment._
import mqtt.model.Packet.Publish
import mqtt.model.QoS.QoS0

/**
 * Provide the structure of Publish packet as referred in chapter 3.3.
 */
case object PublishStructure extends PacketStructure[Publish] {
  
  //3.3.1
  private val dup = (p: Publish) => p.duplicate //3.3.1.1
  private val qos = (p: Publish) => p.message.qos //3.3.1.2
  private val retain = (p: Publish) => p.message.retain //3.3.1.3
  
  //3.3.2
  private val topicName = (p: Publish) => p.message.topic //3.3.2.1
  private val packetId = (p: Publish) => p.message.qos match {
    case QoS0 => empty
    case _ => packetIdentifier
  } //3.3.2.2
  
  override val fixedHeader: PacketFragment[Publish] = controlPacketType(3) :: dup :: qos :: retain :: remainingLength //3.3.1
  override val variableHeader: PacketFragment[Publish] = topicName :: packetId //3.3.2
  override val payload: PacketFragment[Publish] = rawBytes from ((p: Publish) => p.message.payload) //3.3.3
}
