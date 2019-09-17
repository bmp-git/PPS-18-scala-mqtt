package mqtt.broker

import java.util.Calendar

import mqtt.model.Packet.ConnectReturnCode.ConnectionAccepted
import mqtt.model.Packet.{ApplicationMessage, Connack, Connect, Disconnect, Protocol}
import mqtt.model.QoS.QoS0
import mqtt.model.Types.TopicFilter

import scala.concurrent.duration.Duration

object SampleInstances {
  val bs0 = BrokerState(Map(), Map(), Map(), Map())
  
  val sample_id_0 = "123"
  val sample_id_1 = "456"
  val sample_id_2 = "789"
  
  val sample_topic_0 = "abc"
  val sample_topic_1 = "def"
  
  val sample_channel_0 = MQTTChannel(0)
  val sample_channel_1 = MQTTChannel(1)
  val sample_channel_2 = MQTTChannel(2)
  
  val sample_duration_0 = Duration(0, "millis")
  val sample_duration_1 = Duration(10, "minutes")
  
  val sample_connect_packet_0 = Connect(
    protocol = Protocol("MQTT", 4),
    cleanSession = false,
    keepAlive = sample_duration_0,
    clientId = sample_id_0,
    credential = Option.empty,
    willMessage = Option.empty
  )
  
  val sample_disconnect_packet_0 = Disconnect()
  
  val sample_connack_packet_0 = Connack(sessionPresent = false, ConnectionAccepted)
  
  val sample_session_0 = Session(
    channel = Option.empty,
    keepAlive = sample_duration_0,
    lastContact = Calendar.getInstance().getTime,
    subscriptions = Map((TopicFilter(sample_topic_0), QoS0)),
    notYetAcknowledged = Map(),
    receivedButNotYetAcknowledged = Map(),
    pendingTransmission = Seq(),
    persistent = false
  )
  
  
  val sample_session_1 = Session(
    channel = Option.empty,
    keepAlive = sample_duration_0,
    lastContact = Calendar.getInstance().getTime,
    subscriptions = Map(),
    notYetAcknowledged = Map(),
    receivedButNotYetAcknowledged = Map(),
    pendingTransmission = Seq(),
    persistent = false
  )
  
  val sample_session_2 = Session(
    channel = Option.empty,
    keepAlive = sample_duration_1,
    lastContact = Calendar.getInstance().getTime,
    subscriptions = Map(),
    notYetAcknowledged = Map(),
    receivedButNotYetAcknowledged = Map(),
    pendingTransmission = Seq(),
    persistent = false
  )
  
  val sample_application_message_0 = ApplicationMessage(retain = false, QoS0, sample_topic_0, Seq())
  val sample_application_message_1 = ApplicationMessage(retain = false, QoS0, sample_topic_1, Seq())
}
