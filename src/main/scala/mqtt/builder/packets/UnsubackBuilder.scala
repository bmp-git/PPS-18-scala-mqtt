package mqtt.builder.packets

import mqtt.builder.CommonBuilders._
import mqtt.builder.{Builder, IdentityBuilder}
import mqtt.model.Packet.Unsuback

/**
 * The builder of Unsuback packet as referred in chapter 3.11.
 */
case object UnsubackBuilder extends IdentityBuilder[Unsuback] {
  override val builder: Builder[Unsuback] =
    controlPacketType(11) :: (4 zeros) :: remainingLength :: //3.11.1
      packetIdentifier //3.11.2
}
