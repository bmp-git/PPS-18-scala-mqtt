package mqtt.broker

import java.util.Calendar

import mqtt.broker.state.{BrokerState, MQTTChannel, Session}
import mqtt.model.Packet.ConnectReturnCode.ConnectionAccepted
import mqtt.model.Packet._
import mqtt.model.{QoS, TopicFilter}

import scala.concurrent.duration.Duration

object SampleInstances {
  val bs0 = BrokerState(Map(), Map(), Map(), Map())
  
  val sample_id_0 = "123"
  val sample_id_1 = "456"
  val sample_id_2 = "789"
  
  val sample_packet_id_0 = 111
  
  val sample_topic_0 = "sport/tennis"
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
  
  
  val sample_application_message_0 = ApplicationMessage(retain = false, qos = QoS(0), topic = sample_topic_0, payload = "hello".toSeq.map(_.toByte))
  val sample_application_message_1 = ApplicationMessage(retain = false, qos = QoS(0), topic = sample_topic_1, payload = "hello".toSeq.map(_.toByte))
  
  
  val sample_publish_packet_0 = Publish(
    duplicate = false,
    packetId = 0,
    message = sample_application_message_0
  )
  
  
  
  val sample_publish_packet_1 = Publish(
    duplicate = false,
    packetId = 0,
    message = sample_application_message_1
  )
  
  val sample_subscribe_packet_0 = Subscribe(sample_packet_id_0, Seq((sample_topic_0, QoS(0))))
  
  val sample_disconnect_packet_0 = Disconnect()
  
  val sample_connack_packet_0 = Connack(sessionPresent = false, ConnectionAccepted)
  
  val sample_session_0 = Session(
    channel = Option.empty,
    keepAlive = sample_duration_0,
    lastContact = Calendar.getInstance().getTime,
    subscriptions = Map((new TopicFilter(sample_topic_0), QoS(0))),
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

}
