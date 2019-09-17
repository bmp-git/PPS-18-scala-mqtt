package mqtt.builder.packets

import mqtt.builder.PacketStructure
import mqtt.builder.fragments.PacketFragmentImplicits._
import mqtt.builder.fragments.CommonPacketFragments._
import mqtt.builder.fragments.PacketFragment
import mqtt.model.Packet.Connect
import mqtt.utils.Bit
import mqtt.utils.BitImplicits._

/**
 * Provide the structure of Connect packet as referred in chapter 3.1.
 */
case object ConnectStructure extends PacketStructure[Connect] {
  private val protocolName = (p: Connect) => p.protocol.name //3.1.2.1
  private val protocolLevel = (p: Connect) => p.protocol.level.bits.drop(24) //3.1.2.2
  private val cleanSession = (p: Connect) => p.cleanSession //3.1.2.4
  private val willFlag = (p: Connect) => p.willMessage.isDefined //3.1.2.5
  private val willQoS = (p: Connect) => p.willMessage.fold(Seq[Bit](0, 0))(_.qos.bits) //3.1.2.6
  private val willRetain = (p: Connect) => p.willMessage.fold(false)(_.retain) //3.1.2.7
  private val usernameFlag = (p: Connect) => p.credential.isDefined //3.1.2.8
  private val passwordFlag = (p: Connect) => p.credential.fold(false)(_.password.isDefined) //3.1.2.9
  private val flags = usernameFlag :: passwordFlag :: willRetain :: willQoS :: willFlag :: cleanSession :: zero //3.1.2.3
  private val keepAlive = (p: Connect) => p.keepAlive.toSeconds.toInt.bits.drop(16) //3.1.2.10
  private val clientId = (p: Connect) => p.clientId //3.1.3.1
  private val willTopic = (p: Connect) => p.willMessage.map(_.topic) //3.1.3.2
  private val willPayload = (p: Connect) => p.willMessage.map(_.payload) //3.1.3.3
  private val username = (p: Connect) => p.credential.map(_.username) //3.1.3.4
  private val password = (p: Connect) => p.credential.flatMap(_.password) //3.1.3.5
  
  override val fixedHeader: PacketFragment[Connect] = controlPacketType(1) :: (4 zeros):: remainingLength //3.1.1
  
  override val variableHeader: PacketFragment[Connect] = protocolName :: protocolLevel :: flags :: keepAlive //3.1.2
  
  override val payload: PacketFragment[Connect] = clientId :: willTopic :: willPayload :: username :: password //3.1.3
}
