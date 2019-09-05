package mqtt.broker

import java.util.Calendar

import mqtt.model.Packet.{Connect, Disconnect, Protocol}
import mqtt.model.QoS.QoS0
import mqtt.model.Types.TopicFilter
import org.scalatest.FunSuite

import scala.concurrent.duration.Duration

class TestDisconnectPacketHandler extends FunSuite {
  val bs0 = BrokerState(Map(), Map(), Map())
  
  val sample_id_0 = "123"
  
  val sample_topic_0 = "abc"
  
  val sample_socket_0 = Socket(0, Option.empty)
  val sample_socket_1 = Socket(1, Option.empty)
  
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
  
  //TODO refactor
  //TODO move sample instances in a common object shared by test files?
  //TODO add more tests
  
  
  test("Disconnecting with cleanSession 1 should remove the session") {
    val packet = sample_connect_packet_0.copy(cleanSession = true)
    val bs1 = bs0.setSession(sample_id_0, sample_session_0)
    val bs2 = ConnectPacketHandler.handle(bs1, packet, sample_socket_0)
    val bs3 = DisconnectPacketHandler.handle(bs2, sample_disconnect_packet_0, sample_socket_0)
    assert(bs3.sessionFromClientID(sample_id_0).isEmpty)
  }
  
  test("Disconnecting with cleanSession 0 should not remove the session") {
    val packet = sample_connect_packet_0.copy(cleanSession = false)
    val bs1 = bs0.setSession(sample_id_0, sample_session_0)
    val bs2 = ConnectPacketHandler.handle(bs1, packet, sample_socket_0)
    val bs3 = DisconnectPacketHandler.handle(bs2, sample_disconnect_packet_0, sample_socket_0)
    assert(bs3.sessionFromClientID(sample_id_0).isDefined)
  }
}
