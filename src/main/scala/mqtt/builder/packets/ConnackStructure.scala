package mqtt.builder.packets

import mqtt.builder.Builder
import mqtt.builder.BuilderImplicits._
import mqtt.builder.CommonBuilders._
import mqtt.model.Packet.Connack
import mqtt.utils.BitImplicits._

/**
 * Provide the structure of Connack packet as referred in chapter 3.2.
 */
case object ConnackStructure extends PacketStructure[Connack] {
  private val returnCode = (p: Connack) => p.returnCode.value.toByte.bits //Reference table 3.1
  private val sessionPresent = (7 zeros) :: ((p: Connack) => p.sessionPresent) //3.2.2.2
  
  override val builder: Builder[Connack] =
    controlPacketType(2) :: (4 zeros) :: remainingLength :: //3.2.1
      sessionPresent :: returnCode //3.2.2
}
