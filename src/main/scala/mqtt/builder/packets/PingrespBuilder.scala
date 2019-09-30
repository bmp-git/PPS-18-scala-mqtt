package mqtt.builder.packets

import mqtt.builder.CommonBuilders._
import mqtt.builder.{Builder, IdentityBuilder}
import mqtt.model.Packet.Pingresp

/**
 * The builder of Pingresp packet as referred in chapter 3.13.
 */
case object PingrespBuilder extends IdentityBuilder[Pingresp] {
  override val builder: Builder[Pingresp] =
    controlPacketType(13) :: (4 zeros) :: remainingLength //3.13.1
}
