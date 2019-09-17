package mqtt.broker

import mqtt.broker.SampleInstances._
import mqtt.model.Packet.{ApplicationMessage, Connect, Disconnect}
import mqtt.model.QoS.QoS0
import org.scalatest.FunSuite

class TestDisconnect(ConnectPacketHandler: (State, Connect, Socket) => State,
                     DisconnectPacketHandler: (State, Disconnect, Socket) => State) extends FunSuite {
  def connectAndDisconnect(packet: Connect): State = {
    val bs1 = bs0.setSession(sample_id_0, sample_session_0)
    val bs2 = ConnectPacketHandler(bs1, packet, sample_socket_0)
    DisconnectPacketHandler(bs2, sample_disconnect_packet_0, sample_socket_0)
  }
  
  test("Disconnecting with cleanSession 1 should remove the session") {
    val packet = sample_connect_packet_0.copy(cleanSession = true)
    val bs1 = connectAndDisconnect(packet)
    assert(bs1.sessionFromClientID(sample_id_0).isEmpty)
  }
  
  test("Disconnecting with cleanSession 0 should not remove the session") {
    val packet = sample_connect_packet_0.copy(cleanSession = false)
    val bs1 = connectAndDisconnect(packet)
    assert(bs1.sessionFromClientID(sample_id_0).isDefined)
  }
  
  test("A disconnection through a disconnect packet should not publish the will message") {
    val packet = sample_connect_packet_0.copy(willMessage = Option(ApplicationMessage(retain = false, QoS0, sample_topic_0, Seq())))
    val bs1 = ConnectPacketHandler(bs0, packet, sample_socket_0)
    
    //TODO refactor, workaround to check a publish through println
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {
      val bs2 = DisconnectPacketHandler(bs1, sample_disconnect_packet_0, sample_socket_0)
    }
    assert(!stream.toString.contains("Message published ApplicationMessage(false,QoS0,abc,List())"))
  }
}