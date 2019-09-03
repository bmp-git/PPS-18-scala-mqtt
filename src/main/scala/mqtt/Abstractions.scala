package mqtt

import java.net.Socket
import java.util.Date

import mqtt.model.{Packet, PacketID, QoS}
import mqtt.utils.Bit

import scala.concurrent.duration.Duration
import mqtt.model.Types.{ClientID, PackedID, Topic, TopicFilter}
import mqtt.model.Packet.ApplicationMessage


trait Parser[I, O] {
  def parse(input: Seq[I]): O
}

trait Builder[I, O] {
  def build(input: I): Seq[O]
}

trait PacketParser extends Parser[Bit, Packet]

trait PacketBuilder extends Builder[Packet, Bit]

case class Session(
                    clientId: ClientID,
                    socket: Option[Socket],
                    willMessage: Option[ApplicationMessage],
                    keepAlive: Duration,
                    lastContact: Date,
                    subscriptions: Map[TopicFilter, QoS],
                    notYetAcknowledged: Map[PackedID, (Date, Packet with PacketID)], //QoS 1,2
                    receivedButNotYetAcknowledged: Map[PackedID, (Date, Packet with PacketID)], //QoS 2
                    pendingTransmission: Seq[Packet], //QoS 0,1,2
                    persistent: Boolean //[MQTT-3.1.2-6]
                  )

trait State {
  def sessions: Map[ClientID, Session]

  def retains: Map[Topic, ApplicationMessage]

  def session(socket: Socket): Option[Session]

  def setSession(session: Session): State

  def setSocket(socket: Socket, session: Option[Session]): State
}

trait ProtocolManager {
  def handle(state: State, packet: Packet, socket: Socket): State
}

/*trait CommunicationManager {
  def check(state: State):State
}*/