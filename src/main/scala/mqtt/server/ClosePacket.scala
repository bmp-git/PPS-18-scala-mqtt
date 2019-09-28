package mqtt.server

import mqtt.model.Packet

/**
 * Used inside ProtocolHandler inj order to communicate to Sender that the connection must be closed.
 */
case object ClosePacket extends Packet
