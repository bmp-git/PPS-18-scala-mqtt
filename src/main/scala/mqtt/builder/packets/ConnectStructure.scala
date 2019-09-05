package mqtt.builder.packets

import mqtt.builder.PacketStructure
import mqtt.builder.fragments.packetFragmentImplicits._
import mqtt.builder.fragments.commonPacketFragments._
import mqtt.builder.fragments.PacketFragment
import mqtt.model.Packet.Connect
import mqtt.utils.Bit
import mqtt.utils.BitImplicits._


case object ConnectStructure extends PacketStructure[Connect] {
  private val protocolName = (p: Connect) => p.protocol.name
  private val protocolLevel = (p: Connect) => p.protocol.level.bits.drop(24)
  private val usernameFlag = (p: Connect) => p.credential.isDefined
  private val passwordFlag = (p: Connect) => p.credential.fold(false)(_.password.isDefined)
  private val willRetain = (p: Connect) => p.willMessage.fold(false)(_.retain)
  private val willQoS = (p: Connect) => p.willMessage.fold(Seq[Bit](0, 0))(_.qos.bits)
  private val willFlag = (p: Connect) => p.willMessage.isDefined
  private val cleanSession = (p: Connect) => p.cleanSession
  private val flags = usernameFlag | passwordFlag | willRetain | willQoS | willFlag | cleanSession | zero
  private val keepAlive = (p: Connect) => p.keepAlive.toSeconds.toInt.bits.drop(16)
  private val clientId = (p: Connect) => p.clientId
  private val willTopic = (p: Connect) => p.willMessage.map(_.topic)
  private val willPayload = (p: Connect) => p.willMessage.map(_.payload)
  private val username = (p: Connect) => p.credential.map(_.username)
  private val password = (p: Connect) => p.credential.flatMap(_.password)
  
  override val fixedHeader: PacketFragment[Connect] = controlPacketType | zero | zero | zero | zero | remainingLength
  
  override val variableHeader: PacketFragment[Connect] = protocolName | protocolLevel | flags | keepAlive
  
  override val payload: PacketFragment[Connect] = clientId | willTopic | willPayload | username | password
}
