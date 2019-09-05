package mqtt.builder.packets

import mqtt.builder.PacketStructure
import mqtt.builder.fragments.PacketFragment
import mqtt.builder.fragments.commonPacketFragments._
import mqtt.model.Packet.Disconnect


case object DisconnectStructure extends PacketStructure[Disconnect] {
  override val fixedHeader: PacketFragment[Disconnect] = controlPacketType | zero | zero | zero | zero | remainingLength
}
