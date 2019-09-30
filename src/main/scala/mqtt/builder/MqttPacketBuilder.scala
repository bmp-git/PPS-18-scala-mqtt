package mqtt.builder

import mqtt.builder.RichBuilder._
import mqtt.builder.packets._
import mqtt.model.Packet
/**
 * Provides a packet builder for MQTT 3.1.1.
 */
object MqttPacketBuilder extends IdentityBuilder[Packet] {
  val builder: Builder[Packet] =
    ConnectStructure || ConnackStructure || DisconnectStructure ||
      PublishStructure || SubackStructure || SubscribeStructure ||
      PingreqStructure || PingrespStructure || UnsubscribeStructure ||
      UnsubackStructure
}
