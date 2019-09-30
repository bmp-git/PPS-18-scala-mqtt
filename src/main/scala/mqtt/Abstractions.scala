package mqtt

import mqtt.model.Packet
import mqtt.utils.Bit


trait Parser[I, O] {
  def parse(input: Seq[I]): O
}


trait PacketParser extends Parser[Bit, Packet]


/*trait CommunicationManager {
  def check(state: State):State
}*/