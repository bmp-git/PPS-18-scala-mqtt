package mqtt.builder.packets

import mqtt.builder.CommonBuilders._
import mqtt.builder.{Builder, IdentityBuilder}
import mqtt.model.Packet.Pubrec

/**
 * The builder of Pubrec packet as referred in chapter 3.5.
 */
case object PubrecBuilder extends IdentityBuilder[Pubrec] {
  override val builder: Builder[Pubrec] =
    controlPacketType(5) :: (4 zeros) :: remainingLength :: //3.5.1
      packetIdentifier //3.5.2
}
