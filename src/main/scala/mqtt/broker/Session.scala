package mqtt.broker

import java.util.{Calendar, Date}

import mqtt.model.Types.{PackedID, TopicFilter}
import mqtt.model.{Packet, PacketID, QoS}

import scala.concurrent.duration.Duration

//TODO Add credentials to session?
/**
 * Represents the session of a client.
 *
 * @param socket                        the communication channel associated with this session.
 *                                      Could be empty if the client is disconnected but his session is persistent.
 * @param keepAlive                     2/3 of the time after that the client should be forcibly disconnected if no messages arrived.
 * @param lastContact                   the datetime of the last message reception.
 * @param subscriptions                 the client subscriptions.
 * @param notYetAcknowledged            the messages sent but not yet acknowledged from the client (for QoS 1,2).
 * @param receivedButNotYetAcknowledged the partially acknowledged messages (for QoS 2).
 * @param pendingTransmission           the messages that will be sent to the client but not yet transmitted.
 * @param persistent                    tells if the session must be deleted after the client disconnection.
 */
case class Session(
                    socket: Option[Socket],
                    keepAlive: Duration,
                    lastContact: Date,
                    subscriptions: Map[TopicFilter, QoS],
                    notYetAcknowledged: Map[PackedID, (Date, Packet with PacketID)], //QoS 1,2
                    receivedButNotYetAcknowledged: Map[PackedID, (Date, Packet with PacketID)], //QoS 2
                    pendingTransmission: Seq[Packet], //QoS 0,1,2
                    persistent: Boolean //[MQTT-3.1.2-6]
                  )

object Session {
  /**
   * Creates a default empty session, with lastContact equal to the time of creation.
   *
   * @return the session created.
   */
  def createEmptySession(): Session = Session(
    socket = Option.empty,
    keepAlive = Duration(0, "millis"),
    lastContact = Calendar.getInstance().getTime,
    subscriptions = Map(),
    notYetAcknowledged = Map(),
    receivedButNotYetAcknowledged = Map(),
    pendingTransmission = Seq(),
    persistent = false
  )
}
