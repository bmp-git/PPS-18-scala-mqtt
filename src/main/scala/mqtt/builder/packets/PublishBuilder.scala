package mqtt.builder.packets

import mqtt.builder.BuilderImplicits._
import mqtt.builder.CommonBuilders._
import mqtt.builder.RichBuilder._
import mqtt.builder.{Builder, IdentityBuilder}
import mqtt.model.Packet.Publish
import mqtt.model.QoS

/**
 * The builder of Publish packet as referred in chapter 3.3.
 */
case object PublishBuilder extends IdentityBuilder[Publish] {
  //3.3.1
  private val dup = (p: Publish) => p.duplicate //3.3.1.1
  private val qos = (p: Publish) => p.message.qos //3.3.1.2
  private val retain = (p: Publish) => p.message.retain //3.3.1.3
  
  //3.3.2
  private val topicName = (p: Publish) => p.message.topic //3.3.2.1
  private val packetId = (p: Publish) => p.message.qos match {
    case QoS(0) => empty
    case _ => packetIdentifier
  } //3.3.2.2
  
  override val builder: Builder[Publish] =
    controlPacketType(3) :: dup :: qos :: retain :: remainingLength :: //3.3.1
      topicName :: packetId :: //3.3.2
      rawBytes.from[Publish](_.message.payload) //3.3.3
}
