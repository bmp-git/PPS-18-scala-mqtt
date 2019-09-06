package mqtt.builder

import mqtt.PacketBuilder
import mqtt.builder.packets._
import mqtt.model.Packet
import mqtt.model.Packet._
import mqtt.utils.Bit

/**
 * Provides a packet builder for MQTT 3.1.1
 */
object MqttPacketBuilder extends PacketBuilder {
  /**
   * Transforms an input packet to sequence of bits.
   *
   * @param input a MQTT 3.1.1 packet, it can be:
   *              Connect, Connack, Disconnect
   * @return The sequence of bits that encode the specified input
   */
  override def build(input: Packet): Seq[Bit] = input match {
    case packet: Connect => ConnectStructure.build(packet)
    case packet: Connack => ConnackStructure.build(packet)
    case packet: Disconnect => DisconnectStructure.build(packet)
  }
}
