package mqtt.parser

import mqtt.PacketParser
import mqtt.model.ErrorPacket.MalformedPacket
import mqtt.model.Packet
import mqtt.parser.Parsers.MqttParser._

import mqtt.utils.Bit

object MqttPacketParser extends PacketParser {
  override def parse(input: Seq[Bit]): Packet = Parsers.parse(mqttParser(), input) getOrElse MalformedPacket
}