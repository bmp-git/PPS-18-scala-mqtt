package mqtt.broker

import mqtt.model.Packet

trait PacketHandler[T <: Packet] {
  def handle(state: State, packet: T, socket: Socket): State
}
