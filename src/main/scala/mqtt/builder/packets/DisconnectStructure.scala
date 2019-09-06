package mqtt.builder.packets

import mqtt.builder.PacketStructure
import mqtt.builder.fragments.PacketFragment
import mqtt.builder.fragments.CommonPacketFragments._
import mqtt.model.Packet.Disconnect

/**
 * Provide the structure of Disconnect packet as referred in chapter 3.14.
 */
case object DisconnectStructure extends PacketStructure[Disconnect] {
  override val fixedHeader: PacketFragment[Disconnect] = controlPacketType :: zero :: zero :: zero :: zero :: remainingLength //3.14.1
}
