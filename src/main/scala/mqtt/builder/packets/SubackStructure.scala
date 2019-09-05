package mqtt.builder.packets

import mqtt.builder.PacketStructure
import mqtt.builder.fragments.CommonPacketFragments._
import mqtt.builder.fragments.PacketFragment
import mqtt.model.Packet.Suback
import mqtt.model.QoS._
import mqtt.builder.fragments.PacketFragmentImplicits._
import mqtt.utils.BitImplicits._


object SubackStructure extends PacketStructure[Suback] {
  override def fixedHeader: PacketFragment[Suback] = ControlPacketType | Zero | Zero | Zero | Zero | RemainingLength
  
  override def variableHeader: PacketFragment[Suback] = PacketIdentifier
  
  override def payload: PacketFragment[Suback] = (p: Suback) => p.subscriptions.map {
    //3.9.3
    case Some(QoS0) => 0x00
    case Some(QoS1) => 0x01
    case Some(QoS2) => 0x02
    case None => 0x80
  }.flatMap(_.toByte.bits)
}
