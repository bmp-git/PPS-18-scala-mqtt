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
  private val sessionPresent = zero :: zero :: zero :: zero :: zero :: zero :: zero :: ((p: Connack) => p.sessionPresent) //3.2.2.2
  
  override val fixedHeader: PacketFragment[Connack] = controlPacketType :: zero :: zero :: zero :: zero :: remainingLength //3.2.1
  
  override val variableHeader: PacketFragment[Connack] = sessionPresent :: returnCode //3.2.2
}
