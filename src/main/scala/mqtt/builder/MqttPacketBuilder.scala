package mqtt.builder

import mqtt.PacketBuilder
import mqtt.builder.packets._
import mqtt.model.Packet
import mqtt.utils.Bit

/**
 * Provides a packet builder for MQTT 3.1.1.
 */
object MqttPacketBuilder extends PacketBuilder {
  
  //TODO: complete on next sprints
  /**
   * All possible packet's structures
   */
  private val structures = Seq(
    ConnectStructure, ConnackStructure, DisconnectStructure,
    PublishStructure, SubackStructure, SubscribeStructure)
  
  /**
   * Transforms an input packet to sequence of bits.
   *
   * @param input a MQTT 3.1.1 packet, it can be:
   *              Connect, Connack, Disconnect
   * @return The sequence of bits that encode the specified input
   */
  override def build(input: Packet): Seq[Bit] = {
    for (ps <- structures; bits <- ps.build(input)) yield bits
    }.flatten
}
