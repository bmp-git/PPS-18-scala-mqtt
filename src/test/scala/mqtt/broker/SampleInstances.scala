package mqtt.broker

import java.util.Calendar

import mqtt.model.Packet.{Connect, Disconnect, Protocol}
import mqtt.model.QoS.QoS0
import mqtt.model.Types.TopicFilter

import scala.concurrent.duration.Duration

object SampleInstances {
  val bs0 = BrokerState(Map(), Map(), Map())
  
  val sample_id_0 = "123"
  val sample_id_1 = "456"
  val sample_id_2 = "789"
  
  val sample_topic_0 = "abc"
  
  val sample_socket_0 = Socket(0, Option.empty)
  val sample_socket_1 = Socket(1, Option.empty)
  val sample_socket_2 = Socket(2, Option.empty)
  
  val sample_connect_packet_0 = Connect(
    protocol = Protocol("MQTT", 4),
    cleanSession = false,
    keepAlive = Duration(0, "millis"),
    clientId = sample_id_0,
    credential = Option.empty,
    willMessage = Option.empty
  )
  
  val sample_disconnect_packet_0 = Disconnect()
  
  val sample_session_0 = Session(
    socket = Option.empty,
    keepAlive = Duration(0, "millis"),
    lastContact = Calendar.getInstance().getTime,
    subscriptions = Map((TopicFilter(sample_topic_0), QoS0)),
    notYetAcknowledged = Map(),
    receivedButNotYetAcknowledged = Map(),
    pendingTransmission = Seq(),
    persistent = false
  )
  
  
  val sample_session_1 = Session(
    socket = Option.empty,
    keepAlive = Duration(0, "millis"),
    lastContact = Calendar.getInstance().getTime,
    subscriptions = Map(),
    notYetAcknowledged = Map(),
    receivedButNotYetAcknowledged = Map(),
    pendingTransmission = Seq(),
    persistent = false
  )
  
  val sample_session_2 = Session(
    socket = Option.empty,
    keepAlive = Duration(10, "minutes"),
    lastContact = Calendar.getInstance().getTime,
    subscriptions = Map(),
    notYetAcknowledged = Map(),
    receivedButNotYetAcknowledged = Map(),
    pendingTransmission = Seq(),
    persistent = false
  )
  
}
