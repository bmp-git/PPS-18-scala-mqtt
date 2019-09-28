package mqtt.parser

import mqtt.utils.Bit
import mqtt.PacketParser
import mqtt.model.ErrorPacket.MalformedPacket
import mqtt.model.Packet
import mqtt.parser.packets.MqttPacketsParsers.mqtt

/**
 * An MQTT packet parser.
 */
object MqttPacketParser extends PacketParser {
  /**
   * Parse a sequence of bits and return the corresponding MQTT packet.
   *
   * @param input the packet in bits
   * @return the parsed packet
   */
  override def parse(input: Seq[Bit]): Packet = Parsers.parse(mqtt(), input) getOrElse MalformedPacket()
}