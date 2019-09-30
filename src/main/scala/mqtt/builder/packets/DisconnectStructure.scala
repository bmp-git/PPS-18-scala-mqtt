package mqtt.builder.packets

import mqtt.builder.CommonBuilders._
import mqtt.builder.{Builder, IdentityBuilder}
import mqtt.model.Packet.Disconnect

/**
 * Provide the structure of Disconnect packet as referred in chapter 3.14.
 */
case object DisconnectStructure extends IdentityBuilder[Disconnect] {
  override val builder: Builder[Disconnect] =
    controlPacketType(14) :: (4 zeros) :: remainingLength //3.14.1
}
