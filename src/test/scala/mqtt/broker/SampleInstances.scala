package mqtt.broker

import java.util.Calendar

import mqtt.broker.state.{BrokerState, MQTTChannel, Session}
import mqtt.model.Packet.ConnectReturnCode.ConnectionAccepted
import mqtt.model.Packet._
import mqtt.model.{BrokerConfig, QoS, TopicFilter}

import scala.concurrent.duration.Duration

object SampleInstances {
  val bs0 = BrokerState(Map(), Map(), Map(), Map(), Map(), BrokerConfig())
  
  val sample_password_0 = "secretPassword"
  val sample_password_0_bytes: Seq[Byte] = sample_password_0.getBytes("UTF-8").toSeq
  val sample_password_0_digest = "c3c9a0a7ed83ee612c4a3d0501c042778aed5d6399753d7d94ef9ba87c15de1c"
  
  val sample_password_1 = "secretPassword2"
  val sample_password_1_bytes: Seq[Byte] = sample_password_1.getBytes("UTF-8").toSeq
  val sample_password_1_digest = "53b5e46207285560a352b6abbe174a6f71369b882253f6d80868ea9e9ba1f1a3"
  
  val sample_username_0 = "user1"
  val sample_username_1 = "user2"
  val sample_username_2 = "user3"
  
  
  val bs0_auth = BrokerState(Map(), Map(), Map(), Map(), Map(
    sample_username_0 -> Some(sample_password_0_digest),
    sample_username_2 -> None
  ), BrokerConfig(allowAnonymous = false))
  
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
  
  val sample_credential_0 = Some(Credential(sample_username_0, Some(sample_password_0_bytes)))
  val sample_credential_1 = Some(Credential(sample_username_0, Some(sample_password_1_bytes)))
  val sample_credential_2 = Some(Credential(sample_username_1, Some(sample_password_1_bytes)))
  val sample_credential_3 = Some(Credential(sample_username_2, None))
  
  
  val sample_connect_packet_1 = Connect(
    protocol = Protocol("MQTT", 4),
    cleanSession = false,
    keepAlive = sample_duration_0,
    clientId = sample_id_0,
    credential = sample_credential_0,
    willMessage = Option.empty
  )
  
  
  val sample_application_message_0 = ApplicationMessage(retain = false, qos = QoS(0), topic = sample_topic_0, payload = "hello".toSeq.map(_.toByte))
  val sample_application_message_1 = ApplicationMessage(retain = false, qos = QoS(0), topic = sample_topic_1, payload = "hi".toSeq.map(_.toByte))
  
  
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
  
  val sample_unsubscribe_packet_0 = Unsubscribe(sample_packet_id_0, Seq(sample_topic_0))
  val sample_unsubscribe_packet_1 = Unsubscribe(sample_packet_id_0, Seq(sample_topic_1))
  
  
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
