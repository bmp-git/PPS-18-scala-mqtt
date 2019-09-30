package mqtt

import mqtt.model.Packet
import mqtt.utils.Bit




trait Builder[I, O] {
  def build(input: I): Seq[O]
}




trait PacketBuilder extends Builder[Packet, Bit]


/*trait CommunicationManager {
  def check(state: State):State
}*/