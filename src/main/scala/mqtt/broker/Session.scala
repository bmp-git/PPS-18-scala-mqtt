package mqtt.broker

import java.util.{Calendar, Date}

import mqtt.model.{Packet, PacketID, QoS}
import mqtt.model.Packet.ApplicationMessage
import mqtt.model.Types.{PackedID, TopicFilter}

import scala.concurrent.duration.Duration

//TODO Add credentials to session?
case class Session(
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

object Session {
  def createEmptySession(): Session = Session(
    socket = Option.empty,
    willMessage = Option.empty,
    keepAlive = Duration(0, "millis"),
    lastContact = Calendar.getInstance().getTime,
    subscriptions = Map(),
    notYetAcknowledged = Map(),
    receivedButNotYetAcknowledged = Map(),
    pendingTransmission = Seq(),
    persistent = false
  )
}
