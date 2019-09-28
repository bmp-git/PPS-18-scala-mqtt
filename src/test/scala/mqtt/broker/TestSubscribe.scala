package mqtt.broker

import mqtt.broker.SampleInstances._
import mqtt.broker.UtilityFunctions._
import mqtt.broker.state.{Channel, State}
import mqtt.model.Packet.{Publish, Suback, Subscribe}
import mqtt.model.{QoS, Topic}
import org.scalatest.FunSuite

trait TestSubscribe extends FunSuite {
  def SubscribeHandler: (State, Subscribe, Channel) => State
  
  test("A subscribe packet with a legit filter should reply with a suback with a success return code.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_1.copy(channel = Option(sample_channel_0)))
    val bs2 = SubscribeHandler(bs1, sample_subscribe_packet_0, sample_channel_0)
    assertPacketPending(sample_id_0, { case Suback(id, subs) if id == sample_packet_id_0 => subs.contains(Some(QoS(0))) })(bs2)
  }
  
  test("A subscribe packet with a non legit filter should reply with a suback with an error return code.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_1.copy(channel = Option(sample_channel_0)))
    val bs2 = SubscribeHandler(bs1, sample_subscribe_packet_0.copy(topics = Seq((("#sport/"), QoS(0)))), sample_channel_0)
    assertPacketPending(sample_id_0, { case Suback(id, subs) if id == sample_packet_id_0 => subs.contains(None) })(bs2)
  }
  
  test("A subscribe with an empty filter list should disconnect.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_1.copy(channel = Option(sample_channel_0)))
    val bs2 = SubscribeHandler(bs1, sample_subscribe_packet_0.copy(topics = Seq()), sample_channel_0)
    assert(bs2.sessionFromClientID(sample_id_0).isEmpty)
  }
  
  
  test("After a subscription the retain message should be published.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_1.copy(channel = Option(sample_channel_0)))
    val bs2 = bs1.setRetainMessage((new Topic(sample_topic_0)), sample_application_message_0)
    val bs3 = SubscribeHandler(bs2, sample_subscribe_packet_0.copy(topics = Seq(((sample_topic_0), QoS(0)))), sample_channel_0)
    assertPacketPending(sample_id_0, {
      case Publish(dup, _, msg) if !dup && msg.retain => msg.payload == sample_application_message_0.payload && msg.topic == sample_application_message_0.topic
      case _ => false
    })(bs3)
  }
  
}
