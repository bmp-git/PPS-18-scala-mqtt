package mqtt.builder.packets

import mqtt.builder.CommonBuilders._
import mqtt.builder.{Builder, IdentityBuilder}
import mqtt.model.Packet.Pubrel

/**
 * The builder of Pubrel packet as referred in chapter 3.6.
 */
case object PubrelBuilder extends IdentityBuilder[Pubrel] {
  override val builder: Builder[Pubrel] =
    controlPacketType(6) :: zero :: zero :: one :: zero :: remainingLength :: //3.6.1
      packetIdentifier //3.6.2
}
