package mqtt.builder.packets

import mqtt.builder.PacketStructure
import mqtt.builder.fragments.packetFragmentImplicits._
import mqtt.builder.fragments.commonPacketFragments._
import mqtt.builder.fragments.PacketFragment
import mqtt.model.Packet.Connack
import mqtt.utils.BitImplicits._


case object ConnackStructure extends PacketStructure[Connack] {
  override def fixedHeader: PacketFragment[Connack] =
    ControlPacketType | Zero | Zero | Zero | Zero |
      RemainingLength
  
  override def variableHeader: PacketFragment[Connack] =
    Zero | Zero | Zero | Zero | Zero | Zero | Zero | ((p: Connack) => p.sessionPresent) |
      ((p: Connack) => p.returnCode.value.toByte.bits)
}
