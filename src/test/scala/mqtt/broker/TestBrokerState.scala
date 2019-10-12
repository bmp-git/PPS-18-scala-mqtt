package mqtt.broker

import mqtt.broker.SampleInstances._
import mqtt.model.Topic
import org.scalatest.{Assertion, FunSuite}

import scala.concurrent.duration.Duration

class TestBrokerState extends FunSuite {
  
  test("An empty BrokerState should have sessions size 0.") {
    assert(bs0.sessions.isEmpty)
  }
  
  test("An empty BrokerState should have retains size 0.") {
    assert(bs0.retains.isEmpty)
  }
  
  test("Getting a session of a userID from an empty BrokerState should return empty.") {
    assert(bs0.sessionFromClientID(sample_id_1).isEmpty)
  }
  
  test("Getting a session of a channel from an empty BrokerState should return empty.") {
    assert(bs0.sessionFromChannel(sample_channel_1).isEmpty)
  }
  
  test("Storing a session and getting a session of a different userID should return empty.") {
    val bs1 = bs0.setSession(sample_id_1, sample_session_1)
    val s = bs1.sessionFromClientID(sample_id_2)
    assert(s.isEmpty)
  }
  
  test("BrokerState can store a session.") {
    val bs1 = bs0.setSession(sample_id_1, sample_session_1)
    val s = bs1.sessionFromClientID(sample_id_1)
    s.fold(fail)(v => assert(v == sample_session_1))
  }
  
  test("BrokerState can set the channel of a session.") {
    val bs1 = bs0.setSession(sample_id_1, sample_session_1)
    val bs2 = bs1.setChannel(sample_id_1, sample_channel_1)
    val s = bs2.sessionFromChannel(sample_channel_1)
    s.fold(fail)(_._2.channel.fold(fail)(sk => assert(sk == sample_channel_1)))
  }
  
  test("Getting a session from a not present channel should return empty.") {
    val bs1 = bs0.setSession(sample_id_1, sample_session_1)
    val bs2 = bs1.setChannel(sample_id_1, sample_channel_1)
    val s = bs2.sessionFromChannel(sample_channel_2)
    assert(s.isEmpty)
  }
  
  
  test("BrokerState can update a session.") {
    val bs1 = bs0.setSession(sample_id_1, sample_session_1)
    val bs2 = bs1.setSession(sample_id_1, sample_session_2)
    val s = bs2.sessionFromClientID(sample_id_1)
    s.fold(fail)(v => assert(v == sample_session_2))
  }
  
  test("BrokerState can update the channel of a session.") {
    val bs1 = bs0.setSession(sample_id_1, sample_session_1)
    val bs2 = bs1.setChannel(sample_id_1, sample_channel_1)
    val bs3 = bs2.setChannel(sample_id_1, sample_channel_2)
    val s = bs3.sessionFromChannel(sample_channel_2)
    s.fold(fail)(_._2.channel.fold(fail)(sk => assert(sk == sample_channel_2)))
  }
  
  test("BrokerState can add a closing channel.") {
    //there should not be a disconnect in a closingChannel but ok for testing
    val bs1 = bs0.addClosingChannel(sample_channel_0, Seq(sample_disconnect_packet_0))
    bs1.closing.get(sample_channel_0).fold(fail)(pks => assert(pks.contains(sample_disconnect_packet_0)))
  }
  
  test("BrokerState can delete a user session.") {
    val bs1 = bs0.setSession(sample_id_1, sample_session_1)
    val bs2 = bs1.deleteSession(sample_id_1)
    assert(bs2.sessionFromClientID(sample_id_1).isEmpty)
  }
  
  test("BrokerState can update a field of a session from ClientID.") {
    val bs1 = bs0.setSession(sample_id_1, sample_session_1)
    val newKeepAlive = Duration(100, "minutes")
    val bs2 = bs1.updateSessionFromClientID(sample_id_1, s => {
      s.copy(keepAlive = newKeepAlive)
    })
    bs2.sessionFromClientID(sample_id_1).fold(fail)(s => assert(s.keepAlive == newKeepAlive))
  }
  
  test("BrokerState can update a field of a session from channel.") {
    val bs1 = bs0.setSession(sample_id_1, sample_session_1.copy(channel = Some(sample_channel_0)))
    val newKeepAlive = Duration(100, "minutes")
    val bs2 = bs1.updateSessionFromChannel(sample_channel_0, s => {
      s.copy(keepAlive = newKeepAlive)
    })
    bs2.sessionFromClientID(sample_id_1).fold(fail)(s => assert(s.keepAlive == newKeepAlive))
  }
  
  test("BrokerState can save a will message.") {
    val bs1 = bs0.setWillMessage(sample_channel_0, sample_application_message_0)
    bs1.wills.get(sample_channel_0).fold(fail)(m => assert(m == sample_application_message_0))
  }
  
  test("BrokerState can update a will message.") {
    val bs1 = bs0.setWillMessage(sample_channel_0, sample_application_message_0)
    val bs2 = bs1.setWillMessage(sample_channel_0, sample_application_message_1)
    bs2.wills.get(sample_channel_0).fold(fail)(m => assert(m == sample_application_message_1))
  }
  
  test("BrokerState can delete a will message.") {
    val bs1 = bs0.setWillMessage(sample_channel_0, sample_application_message_0)
    val bs2 = bs1.deleteWillMessage(sample_channel_0)
    assert(bs2.wills.isEmpty)
  }
  
  test("BrokerState can take a pending transmission.") {
    val s1 = sample_session_0.copy(channel = Option(sample_channel_0), pendingTransmission = Seq(sample_connack_packet_0))
    val bs1 = bs0.setSession(sample_id_0, s1)
    val (_, packets) = bs1.takeAllPendingTransmission
    packets.get(sample_channel_0).fold(fail)(seq => assert(seq.contains(sample_connack_packet_0)))
  }
  
  test("After a take the pending transmissions are removed from the session.") {
    val s1 = sample_session_0.copy(channel = Option(sample_channel_0), pendingTransmission = Seq(sample_connack_packet_0))
    val bs1 = bs0.setSession(sample_id_0, s1)
    val (bs2, _) = bs1.takeAllPendingTransmission
    bs2.sessionFromChannel(sample_channel_0).fold[Assertion](fail){case (_, s) => assert(s.pendingTransmission.isEmpty)}
  }
  
  test("Pending transmissions of a non active session are not taken.") {
    val s1 = sample_session_0.copy(pendingTransmission = Seq(sample_connack_packet_0))
    val bs1 = bs0.setSession(sample_id_0, s1)
    val (_, packets) = bs1.takeAllPendingTransmission
    assert(packets.isEmpty)
  }
  
  test("Pending transmissions of a non active session are not deleted.") {
    val s1 = sample_session_0.copy(pendingTransmission = Seq(sample_connack_packet_0))
    val bs1 = bs0.setSession(sample_id_0, s1)
    val (bs2, _) = bs1.takeAllPendingTransmission
    bs2.sessionFromClientID(sample_id_0).fold(fail)(s => assert(s.pendingTransmission.contains(sample_connack_packet_0)))
  }
  
  test("BrokerState can save a retain message.") {
    val bs1 = bs0.setRetainMessage(new Topic(sample_topic_0), sample_application_message_0)
    bs1.retains.get(new Topic(sample_topic_0)).fold(fail)(m => assert(m == sample_application_message_0))
  }
  
  test("BrokerState can update a retain message.") {
    val bs1 = bs0.setRetainMessage(new Topic(sample_topic_0), sample_application_message_0)
    val bs2 = bs1.setRetainMessage(new Topic(sample_topic_0), sample_application_message_1)
    bs2.retains.get(new Topic(sample_topic_0)).fold(fail)(m => assert(m == sample_application_message_1))
  }
  
  test("BrokerState can delete a retain message.") {
    val bs1 = bs0.setRetainMessage(new Topic(sample_topic_0), sample_application_message_0)
    val bs2 = bs1.clearRetainMessage(new Topic(sample_topic_0))
    assert(bs2.retains.isEmpty)
  }
  
  test("BrokerState can take closing channels.") {
    val bs1 = bs0.addClosingChannel(sample_channel_0, Seq(sample_disconnect_packet_0))
    val bs2 = bs1.addClosingChannel(sample_channel_1, Seq(sample_disconnect_packet_0))
    
    val (bs3, closing) = bs2.takeClosing
    
    assert(closing.size == 2)
    assert(bs3.closing.isEmpty)
  }
}
