package mqtt.broker

import mqtt.broker.SampleInstances._
import org.scalatest.FunSuite

class TestDisconnectPacketHandler extends FunSuite {
  
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
