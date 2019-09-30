package mqtt.builder.packets

import mqtt.builder.CommonBuilders._
import mqtt.builder.{Builder, IdentityBuilder}
import mqtt.model.Packet.Pubcomp

/**
 * The builder of Pubcomp packet as referred in chapter 3.7.
 */
case object PubcompBuilder extends IdentityBuilder[Pubcomp] {
  override val builder: Builder[Pubcomp] =
    controlPacketType(7) :: (4 zeros) :: remainingLength :: //3.7.1
      packetIdentifier //3.7.2
}
