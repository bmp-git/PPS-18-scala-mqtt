package mqtt.parser

import mqtt.PacketParser
import mqtt.model.ErrorPacket.MalformedPacket
import mqtt.model.Packet
import mqtt.parser.Parsers.MqttParser._

import mqtt.utils.Bit

class PacketParserImpl extends PacketParser {
  override def parse(input: Seq[Bit]): Packet = Parsers.parse(disconnectParser(), input) getOrElse MalformedPacket
}

object PacketParser {
  def apply(): PacketParser = new PacketParserImpl()
}