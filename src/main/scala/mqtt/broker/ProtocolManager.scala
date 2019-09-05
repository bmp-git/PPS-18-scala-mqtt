package mqtt.broker

import mqtt.model.Packet

trait ProtocolManager {
  def handle(state: State, packet: Packet, socket: Socket): State
}
