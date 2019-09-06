package mqtt.broker

import mqtt.broker.SampleInstances._
import org.scalatest.FunSuite

import scala.concurrent.duration.Duration

class TestBrokerState extends FunSuite {
  
  
  //TODO addClosingChannel test
  //TODO add tests for yet untested methods
  
  test("An empty BrokerState should have sessions size 0") {
    assert(bs0.sessions.isEmpty)
  }
  
  test("An empty BrokerState should have retains size 0") {
    assert(bs0.retains.isEmpty)
  }
  
  test("Getting a session of a userID from an empty BrokerState should return empty") {
    assert(bs0.sessionFromClientID(sample_id_1).isEmpty)
  }
  
  test("Getting a session of a socket from an empty BrokerState should return empty") {
    assert(bs0.sessionFromSocket(sample_socket_1).isEmpty)
  }
  
  test("Storing a session and getting a session of a different userID should return empty") {
    val bs1 = bs0.setSession(sample_id_1, sample_session_1)
    val s = bs1.sessionFromClientID(sample_id_2)
    assert(s.isEmpty)
  }
  
  test("BrokerState can store a session") {
    val bs1 = bs0.setSession(sample_id_1, sample_session_1)
    val s = bs1.sessionFromClientID(sample_id_1)
    s.fold(fail)(v => assert(v == sample_session_1))
  }
  
  test("BrokerState can set the socket of a session") {
    val bs1 = bs0.setSession(sample_id_1, sample_session_1)
    val bs2 = bs1.setSocket(sample_id_1, sample_socket_1)
    val s = bs2.sessionFromSocket(sample_socket_1)
    s.fold(fail)(_._2.socket.fold(fail)(sk => assert(sk == sample_socket_1)))
  }
  
  test("Getting a session from a not present socket should return empty") {
    val bs1 = bs0.setSession(sample_id_1, sample_session_1)
    val bs2 = bs1.setSocket(sample_id_1, sample_socket_1)
    val s = bs2.sessionFromSocket(sample_socket_2)
    assert(s.isEmpty)
  }
  
  
  test("BrokerState can update a session") {
    val bs1 = bs0.setSession(sample_id_1, sample_session_1)
    val bs2 = bs1.setSession(sample_id_1, sample_session_2)
    val s = bs2.sessionFromClientID(sample_id_1)
    s.fold(fail)(v => assert(v == sample_session_2))
  }
  
  test("BrokerState can update the socket of a session") {
    val bs1 = bs0.setSession(sample_id_1, sample_session_1)
    val bs2 = bs1.setSocket(sample_id_1, sample_socket_1)
    val bs3 = bs2.setSocket(sample_id_1, sample_socket_2)
    val s = bs3.sessionFromSocket(sample_socket_2)
    s.fold(fail)(_._2.socket.fold(fail)(sk => assert(sk == sample_socket_2)))
  }
  
  test("BrokerState can add a closing channel") {
    //there should not be a disconnect in a closingChannel but ok for testing
    val bs1 = bs0.addClosingChannel(sample_socket_0, Seq(sample_disconnect_packet_0))
    bs1.closing.get(sample_socket_0).fold(fail)(pks => assert(pks.contains(sample_disconnect_packet_0)))
  }
  
  test("BrokerState can delete a user session") {
    val bs1 = bs0.setSession(sample_id_1, sample_session_1)
    val bs2 = bs1.deleteUserSession(sample_id_1)
    assert(bs2.sessionFromClientID(sample_id_1).isEmpty)
  }
  
  test("BrokerState can update a field of a session") {
    val bs1 = bs0.setSession(sample_id_1, sample_session_1)
    val newKeepAlive = Duration(100, "minutes")
    val bs2 = bs1.updateUserSession(sample_id_1, s => {
      s.copy(keepAlive = newKeepAlive)
    })
    bs2.sessionFromClientID(sample_id_1).fold(fail)(s => assert(s.keepAlive == newKeepAlive))
  }
}
