package mqtt.builder

import mqtt.builder.RichBuilder._
import mqtt.builder.packets._
import mqtt.model.Packet
import mqtt.model.Packet._
/**
 * Provides a packet builder for MQTT 3.1.1.
 */
object MqttPacketBuilder extends IdentityBuilder[Packet] {
  val builder: Builder[Packet] =
    ConnectBuilder
      .||[Packet, Connack](ConnackBuilder)
      .||[Packet, Disconnect](DisconnectBuilder)
      .||[Packet, Publish](PublishBuilder)
      .||[Packet, Suback](SubackBuilder)
      .||[Packet, Subscribe](SubscribeBuilder)
      .||[Packet, Pingreq](PingreqBuilder)
      .||[Packet, Pingresp](PingrespBuilder)
      .||[Packet, Unsubscribe](UnsubscribeBuilder)
      .||[Packet, Unsuback](UnsubackBuilder)
      .||[Packet, Pubrec](PubrecBuilder)
      .||[Packet, Pubcomp](PubcompBuilder)
      .||[Packet, Pubrel](PubrelBuilder)
      .||[Packet, Puback](PubackBuilder)
}