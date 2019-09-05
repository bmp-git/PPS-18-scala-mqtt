package mqtt.builder.packets

import mqtt.builder.PacketStructure
import mqtt.builder.fragments.packetFragmentImplicits._
import mqtt.builder.fragments.commonPacketFragments._
import mqtt.builder.fragments.PacketFragment
import mqtt.model.Packet.Connack
import mqtt.utils.BitImplicits._


case object ConnackStructure extends PacketStructure[Connack] {
  private val returnCode = (p: Connack) => p.returnCode.value.toByte.bits
  private val sessionPresent = zero | zero | zero | zero | zero | zero | zero | ((p: Connack) => p.sessionPresent)
  
  override val fixedHeader: PacketFragment[Connack] = controlPacketType | zero | zero | zero | zero | remainingLength
  
  override val variableHeader: PacketFragment[Connack] = sessionPresent | returnCode
}
