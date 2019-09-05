package mqtt.builder.packets

import mqtt.builder.PacketStructure
import mqtt.builder.fragments.packetFragmentImplicits._
import mqtt.builder.fragments.commonPacketFragments._
import mqtt.builder.fragments.PacketFragment
import mqtt.model.Packet.Connect
import mqtt.utils.Bit
import mqtt.utils.BitImplicits._


case object ConnectStructure extends PacketStructure[Connect] {
  override def fixedHeader: PacketFragment[Connect] =
    ControlPacketType | Zero | Zero | Zero | Zero |
      RemainingLength
  
  override def variableHeader: PacketFragment[Connect] =
    ((p: Connect) => p.protocol.name) |
      ((p: Connect) => p.protocol.level.bits.drop(24)) |
      ((p: Connect) => p.credential.isDefined) |
      ((p: Connect) => p.credential.fold(false)(_.password.isDefined)) |
      ((p: Connect) => p.willMessage.fold(false)(_.retain)) |
      ((p: Connect) => p.willMessage.fold(Seq[Bit](0,0))(_.qos.bits)) |
      ((p: Connect) => p.willMessage.isDefined) |
      ((p: Connect) => p.cleanSession) |
      Zero |
      ((p: Connect) => p.keepAlive.toSeconds.toInt.bits.drop(16))
  
  override def payload: PacketFragment[Connect] =
    ((p: Connect) => p.clientId) |
      ((p: Connect) => p.willMessage.map(_.topic)) |
      ((p: Connect) => p.willMessage.map(_.payload)) |
      ((p: Connect) => p.credential.map(_.username)) |
      ((p: Connect) => p.credential.flatMap(_.password))
}
