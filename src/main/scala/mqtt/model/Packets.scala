package mqtt.model

import mqtt.model.Types._

import scala.concurrent.duration.Duration

trait Packet

trait PacketID {
  def packetId: PackedID
}

object ErrorPacket { //is this ok?
  
  case class MalformedPacket() extends Packet
  
  case class ChannelClosed() extends Packet
}

object Packet {
  
  case class Credential(username: String, password: Option[Password])
  
  case class Protocol(name: String, level: Int)
  
  case class ApplicationMessage(retain: Boolean, qos: QoS, topic: String, payload: Payload)
  
  /* 3.2.2.3 */
  sealed abstract class ConnectReturnCode(val value: Int)
  
  object ConnectReturnCode {
    
    case object ConnectionAccepted extends ConnectReturnCode(0)
    
    case object UnacceptableProtocolVersion extends ConnectReturnCode(1)
    
    case object IdentifierRejected extends ConnectReturnCode(2)
    
    case object ServerUnavailable extends ConnectReturnCode(3)
    
    case object BadUsernameOrPassword extends ConnectReturnCode(4)
    
    case object NotAuthorized extends ConnectReturnCode(5)
  
    def apply(value: Int): ConnectReturnCode = value match {
      case 0 => ConnectionAccepted
      case 1 => UnacceptableProtocolVersion
      case 2 => IdentifierRejected
      case 3 => ServerUnavailable
      case 4 => BadUsernameOrPassword
      case 5 => NotAuthorized
    }
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
  case class Pingreq() extends Packet
  
  /* 3.13 */
  case class Pingresp() extends Packet
  
  /* 3.14 */
  case class Disconnect() extends Packet
}