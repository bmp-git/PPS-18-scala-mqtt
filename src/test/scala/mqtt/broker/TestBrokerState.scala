package mqtt.broker

import java.util.Calendar

import org.scalatest.FunSuite

import scala.concurrent.duration.Duration

class TestBrokerState extends FunSuite {
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
  
  val sample_id_1 = "123"
  val sample_id_2 = "456"
  
  val sample_socket_1 = Socket(1, Option.empty)
  val sample_socket_2 = Socket(2, Option.empty)
  
  val bs0 = BrokerState(Map(), Map(), Map())
  
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
}
