package mqtt

import mqtt.model.Packet
import mqtt.utils.Bit


trait Parser[I, O] {
  def parse(input: Seq[I]): O
}

trait Builder[I, O] {
  def build(input: I): Seq[O]
}



trait PacketParser extends Parser[Bit, Packet]

trait PacketBuilder extends Builder[Packet, Bit]


/*trait CommunicationManager {
  def check(state: State):State
}*/