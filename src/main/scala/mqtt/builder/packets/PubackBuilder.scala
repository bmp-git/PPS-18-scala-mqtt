package mqtt.builder.packets

import mqtt.builder.CommonBuilders._
import mqtt.builder.{Builder, IdentityBuilder}
import mqtt.model.Packet.Puback

/**
 * The builder of Puback packet as referred in chapter 3.4.
 */
case object PubackBuilder extends IdentityBuilder[Puback] {
  override val builder: Builder[Puback] =
    controlPacketType(4) :: (4 zeros) :: remainingLength :: //3.4.1
      packetIdentifier //3.4.2
}
