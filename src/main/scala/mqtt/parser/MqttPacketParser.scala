package mqtt.parser

import mqtt.utils.Bit
import mqtt.model.ErrorPacket.MalformedPacket
import mqtt.model.Packet
import mqtt.parser.packets.MqttPacketsParsers.mqtt

/**
 * Represents a Packet parser. A Packet parser is a parser that takes a sequence of bits and produce a packet.
 */
trait PacketParser extends Parser[Bit, Packet]

/**
 * A MQTT packet parser.
 */
object MqttPacketParser extends PacketParser {
  /**
   * Parse a sequence of bits and return the corresponding MQTT packet.
   *
   * @param input the packet in bits
   * @return the correctly parsed packet, MalformedPacket otherwise
   */
  override def parse(input: Seq[Bit]): Packet = Parsers.parse(mqtt(), input) getOrElse MalformedPacket()
}