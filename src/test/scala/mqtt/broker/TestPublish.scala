package mqtt.broker

import mqtt.broker.SampleInstances._
import mqtt.broker.UtilityFunctions._
import mqtt.broker.state.{Channel, State}
import mqtt.model.Packet.Publish
import mqtt.model.Topic
import org.scalatest.FunSuite

trait TestPublish extends FunSuite {
  def PublishHandler: (State, Publish, Channel) => State
  
  test("A publish with a bad topic name should disconnect.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_0.copy(channel = Option(sample_channel_0)))
    val packet = sample_publish_packet_0.copy(message = sample_publish_packet_0.message.copy(topic = "#"))
    val bs2 = PublishHandler(bs1, packet, sample_channel_0)
    assertDisconnected(sample_id_0)(bs2)
  }
  
  test("A publish with a bad dup qos pair should disconnect.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_0.copy(channel = Option(sample_channel_0)))
    val packet = sample_publish_packet_0.copy(duplicate = true)
    val bs2 = PublishHandler(bs1, packet, sample_channel_0)
    assertDisconnected(sample_id_0)(bs2)
  }
  
  test("A publish should be forwarded to a client with a matching subscription.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_0.copy(channel = Option(sample_channel_0)))
    val bs2 = bs1.setSession(sample_id_1, sample_session_1.copy(channel = Option(sample_channel_1)))
    val bs3 = PublishHandler(bs2, sample_publish_packet_0, sample_channel_1)
    assertPacketPending(sample_id_0, _ == sample_publish_packet_0)(bs3)
  }
  
  test("A publish should not be forwarded to a client with a non matching subscription.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_0.copy(channel = Option(sample_channel_0)))
    val bs2 = bs1.setSession(sample_id_1, sample_session_1.copy(channel = Option(sample_channel_1)))
    val bs3 = PublishHandler(bs2, sample_publish_packet_1, sample_channel_1)
    assertPendingEmpty(sample_id_0)(bs3)
  }
  
  test("A publish with retain should be saved.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_0.copy(channel = Option(sample_channel_0)))
    val packet = sample_publish_packet_0.copy(message = sample_publish_packet_0.message.copy(retain = true))
    val bs2 = PublishHandler(bs1, packet, sample_channel_0)
    bs2.retains.get(new Topic(sample_topic_0)).fold(fail)(m => assert(m.topic == sample_publish_packet_0.message.topic))
  }
}
