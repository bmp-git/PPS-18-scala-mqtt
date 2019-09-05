package mqtt.builder

import mqtt.PacketBuilder
import mqtt.builder.packets._
import mqtt.model.Packet
import mqtt.model.Packet._
import mqtt.utils.Bit


object MqttPacketBuilder extends PacketBuilder {
  override def build(input: Packet): Seq[Bit] = input match {
    case packet: Connect => ConnectStructure.build(packet)
    case packet: Connack => ConnackStructure.build(packet)
    case packet: Disconnect => DisconnectStructure.build(packet)
  }
}
