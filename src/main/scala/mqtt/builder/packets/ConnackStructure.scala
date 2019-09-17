package mqtt.builder.packets

import mqtt.builder.PacketStructure
import mqtt.builder.fragments.PacketFragmentImplicits._
import mqtt.builder.fragments.CommonPacketFragments._
import mqtt.builder.fragments.PacketFragment
import mqtt.model.Packet.Connack
import mqtt.utils.BitImplicits._

/**
 * Provide the structure of Connack packet as referred in chapter 3.2.
 */
case object ConnackStructure extends PacketStructure[Connack] {
  private val returnCode = (p: Connack) => p.returnCode.value.toByte.bits //Reference table 3.1
  private val sessionPresent = (7 zeros) :: ((p: Connack) => p.sessionPresent) //3.2.2.2
  
  override val fixedHeader: PacketFragment[Connack] = controlPacketType(2) :: (4 zeros) :: remainingLength //3.2.1
  
  override val variableHeader: PacketFragment[Connack] = sessionPresent :: returnCode //3.2.2
}
