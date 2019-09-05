package mqtt.model

import scala.concurrent.duration.Duration
import Types._

trait Packet

trait PacketID {
  def packetId: PackedID
}

object ErrorPacket { //is this ok?

  trait CloseConnection extends Packet

  case object ProtocolError extends CloseConnection

  case object MalformedPacket extends CloseConnection

}

object Packet {

  case class Credential(username: String, password: Option[Password])

  case class Protocol(name: String, level: Int)

  case class ApplicationMessage(retain: Boolean, qos: QoS, topic: String, payload: Payload)

  /* 3.2.2.3 */
  sealed trait ConnectReturnCode

  object ConnectReturnCode {

    case object ConnectionAccepted extends ConnectReturnCode

    case object UnacceptableProtocolVersion extends ConnectReturnCode

    case object IdentifierRejected extends ConnectReturnCode

    case object ServerUnavailable extends ConnectReturnCode

    case object BadUsernameOrPassword extends ConnectReturnCode

    case object NotAuthorized extends ConnectReturnCode

  }

  /* 3.1 */
  case class Connect(
                      protocol: Protocol,
                      cleanSession: Boolean,
                      keepAlive: Duration,
                      clientId: ClientID,
                      credential: Option[Credential],
                      willMessage: Option[ApplicationMessage]
                    ) extends Packet

  /* 3.2 */
  case class Connack(
                      sessionPresent: Boolean,
                      returnCode: ConnectReturnCode
                    ) extends Packet

  /* 3.3 */
  case class Publish(
                      duplicate: Boolean,
                      packetId: PackedID,
                      message: ApplicationMessage
                    ) extends Packet with PacketID

  /* 3.4 */
  case class Puback(packetId: PackedID) extends Packet with PacketID

  /* 3.5 */
  case class Pubrec(packetId: PackedID) extends Packet with PacketID

  /* 3.6 */
  case class Pubrel(packetId: PackedID) extends Packet with PacketID

  /* 3.7 */
  case class Pubcomp(packetId: PackedID) extends Packet with PacketID

  /* 3.8 */
  case class Subscribe(
                        packetId: PackedID,
                        topics: Seq[(TopicFilter, QoS)]
                      ) extends Packet with PacketID

  /* 3.9 */
  case class Suback(
                     packetId: PackedID,
                     subscriptions: Seq[Option[QoS]]
                   ) extends Packet with PacketID

  /* 3.10 */
  case class Unsubscribe(
                          packetId: PackedID,
                          topics: Seq[TopicFilter]
                        ) extends Packet with PacketID

  /* 3.11 */
  case class Unsuback(packetId: PackedID) extends Packet with PacketID

  /* 3.12 */
  case object Pingreq extends Packet

  /* 3.13 */
  case object Pingresp extends Packet

  /* 3.14 */
  case class Disconnect() extends Packet

}