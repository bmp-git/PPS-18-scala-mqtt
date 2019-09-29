package mqtt.broker

import mqtt.broker.SampleInstances._
import mqtt.broker.UtilityFunctions.assertPacketNotPending
import mqtt.broker.state.{Channel, State}
import mqtt.model.Packet.{ApplicationMessage, Connect, Disconnect, Publish}
import mqtt.model.QoS
import org.scalatest.FunSuite

trait TestDisconnect extends FunSuite {
  
  def ConnectHandler: (State, Connect, Channel) => State
  
  def DisconnectHandler: (State, Disconnect, Channel) => State
  
  def connectAndDisconnect(packet: Connect): State = {
    val bs1 = bs0.setSession(sample_id_0, sample_session_0)
    val bs2 = ConnectHandler(bs1, packet, sample_channel_0)
    DisconnectHandler(bs2, sample_disconnect_packet_0, sample_channel_0)
  }
  
  test("Disconnecting with cleanSession 1 should remove the session.") {
    val packet = sample_connect_packet_0.copy(cleanSession = true)
    val bs1 = connectAndDisconnect(packet)
    assert(bs1.sessionFromClientID(sample_id_0).isEmpty)
  }
  
  test("Disconnecting with cleanSession 0 should not remove the session.") {
    val packet = sample_connect_packet_0.copy(cleanSession = false)
    val bs1 = connectAndDisconnect(packet)
    assert(bs1.sessionFromClientID(sample_id_0).isDefined)
  }
  
  test("A disconnection through a disconnect packet should not publish the will message.") {
    val applicationMessage = ApplicationMessage(retain = false, QoS(0), sample_topic_0, Seq())
    val packet = sample_connect_packet_0.copy(clientId = sample_id_1, willMessage = Option(applicationMessage))
    
    val bs1 = bs0.setSession(sample_id_0, sample_session_0.copy(channel = Option(sample_channel_0))) //he is subscribed to topic0
    val bs2 = ConnectHandler(bs1, packet, sample_channel_1) //sets will message
    val bs3 = DisconnectHandler(bs2, sample_disconnect_packet_0, sample_channel_1) //will disconnect, will not published
    
    assertPacketNotPending(sample_id_0, {
      case p: Publish => p.message == applicationMessage
      case _ => false
    })(bs3)
  }
}
