package mqtt.builder

import mqtt.PacketBuilder
import mqtt.builder.packets.ConnackStructure
import mqtt.model.Packet
import mqtt.model.Packet.Connack
import mqtt.utils.Bit

object MqttPacketBuilder extends PacketBuilder {
  override def build(input: Packet): Seq[Bit] = input match {
    case packet: Connack => ConnackStructure.build(packet)
  }
}
