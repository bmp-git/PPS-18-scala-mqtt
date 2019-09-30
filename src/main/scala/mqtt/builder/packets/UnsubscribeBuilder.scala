package mqtt.builder.packets

import mqtt.builder.CommonBuilders._
import mqtt.builder.RichBuilder._
import mqtt.builder.{Builder, IdentityBuilder}
import mqtt.model.Packet.Unsubscribe

/**
 * Provide the structure of Unsubscribe packet as referred in chapter 3.10.
 */
case object UnsubscribeBuilder extends IdentityBuilder[Unsubscribe] {
  override val builder: Builder[Unsubscribe] =
    controlPacketType(10) :: zero :: zero :: one :: zero :: remainingLength :: //3.10.1
      packetIdentifier :: //3.10.2
      stringBuilder.foreach((p: Unsubscribe) => p.topics) //3.10.3
}
