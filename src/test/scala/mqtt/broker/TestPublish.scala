package mqtt.broker

import mqtt.broker.SampleInstances._
import mqtt.broker.UtilityFunctions._
import mqtt.broker.state.{Channel, State}
import mqtt.model.Packet.Publish
import mqtt.model.{QoS, Topic, TopicFilter}
import org.scalatest.FunSuite

trait TestPublish extends FunSuite {
  def PublishHandler: (State, Publish, Channel) => State
  
  //TODO remove when QoS2 will be supported.
  test("A publish with an unsupported QoS should disconnect.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_0.copy(channel = Option(sample_channel_0)))
    val packet = sample_publish_packet_0.copy(message = sample_publish_packet_0.message.copy(qos = QoS(2)))
    val bs2 = PublishHandler(bs1, packet, sample_channel_0)
    assertDisconnected(sample_id_0)(bs2)
  }
  
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
  
  test("A publish with retain and an empty payload should be published and should clear retains for that topic.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_0.copy(channel = Option(sample_channel_0))) //client 1 subscribed to sport/tennis
    val bs2 = bs1.setSession(sample_id_1, sample_session_1.copy(channel = Option(sample_channel_1))) //client 2 empty subscriptions
    val bs3 = bs2.setRetainMessage(new Topic(sample_topic_0), sample_application_message_0) //set retain for sport/tennis
    
    //client 2 publishes a new retain for sport/tennis
    val packet = sample_publish_packet_0.copy(message = sample_publish_packet_0.message.copy(retain = true, payload = Seq()))
    val bs4 = PublishHandler(bs3, packet, sample_channel_1)
    
    //assert retain are cleared for sport/tennis
    assert(bs4.retains.get(new Topic(sample_topic_0)).isEmpty)
    
    //assert publish arrived to client 1
    assertPacketPending(sample_id_0, {
      case Publish(_, _, msg) => msg.payload.isEmpty && msg.topic == sample_topic_0
      case _ => false
    })(bs4)
  }
  
  /* TODO cannot test with only QoS0 support, uncomment when QoS1 will be supported.
  test("DUP flag is not propagated.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_0.copy(channel = Option(sample_channel_0))) //client 1 subscribed to sport/tennis
    val bs2 = bs1.setSession(sample_id_1, sample_session_1.copy(channel = Option(sample_channel_1))) //client 2 empty subscriptions
    
    //client 2 publishes a packet with duplicate 1
    val packet = sample_publish_packet_0.copy(duplicate = true, message = sample_publish_packet_0.message.copy(qos = QoS(1)))
    val bs4 = PublishHandler(bs2, packet, sample_channel_1)
    
    //assert publish arrived to client 1 with duplicate 0
    assertPacketPending(sample_id_0, {
      case Publish(dup, _, msg) if !dup => msg.topic == sample_topic_0
      case _ => false
    })(bs4)
  }
  */
  
  test("A QoSO publish with retain replaces the old retain message.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_0.copy(channel = Option(sample_channel_0)))
    val bs2 = bs1.setRetainMessage(new Topic(sample_topic_0), sample_application_message_0)
    
    val packet = sample_publish_packet_0.copy(message = sample_publish_packet_1.message.copy(retain = true, topic = sample_topic_0))
    val bs3 = PublishHandler(bs2, packet, sample_channel_0)
    bs3.retains.get(new Topic(sample_topic_0)).fold(fail)(m => assert(m.payload.equals(sample_publish_packet_1.message.payload)))
  }
  
  test("A publish with retain flag false should not modify the retained message for that topic.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_0.copy(channel = Option(sample_channel_0)))
    val bs2 = bs1.setRetainMessage(new Topic(sample_topic_0), sample_application_message_0)
    
    val packet = sample_publish_packet_0.copy(message = sample_publish_packet_1.message.copy(retain = false, topic = sample_topic_0))
    val bs3 = PublishHandler(bs2, packet, sample_channel_0)
    bs3.retains.get(new Topic(sample_topic_0)).fold(fail)(m => assert(m.payload.equals(sample_publish_packet_0.message.payload)))
  }
  
  test("A publish with a true retain flag should propagate retain false to matching subscribers.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_0.copy(channel = Option(sample_channel_0))) //client 1 subscribed to sport/tennis
    val bs2 = bs1.setSession(sample_id_1, sample_session_1.copy(channel = Option(sample_channel_1))) //client 2 empty subscriptions
  
    //client 2 publishes new retain for sport/tennis
    val packet = sample_publish_packet_0.copy(message = sample_publish_packet_0.message.copy(retain = true))
    val bs3 = PublishHandler(bs2, packet, sample_channel_1)
  
    //assert publish arrived to client 1 with retain false
    assertPacketPending(sample_id_0, {
      case Publish(false, _, msg) => msg.topic == sample_topic_0
      case _ => false
    })(bs3)
  }
  
  test("A publish should be sent to all matching subscribers only.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_0.copy(channel = Option(sample_channel_0))) //client 1 subscribed to sport/tennis
    val bs2 = bs1.setSession(sample_id_1, sample_session_1.copy(channel = Option(sample_channel_1))) //client 2 empty subscriptions
    val bs3 = bs2.setSession(sample_id_2, sample_session_0
      .copy(channel = Option(sample_channel_2), subscriptions = Map(new TopicFilter("#") -> QoS(0)))) //client 3 subscribed to #
    
    //client 2 publishes to sport/tennis
    val bs4 = PublishHandler(bs3, sample_publish_packet_0, sample_channel_1)
    
    //assert publish arrived to client 1
    assertPacketPending(sample_id_0, {
      case Publish(_, _, msg) => msg.topic == sample_topic_0
      case _ => false
    })(bs4)
  
    //assert publish not arrived to client 2.
    assertPacketNotPending(sample_id_1, {
      case Publish(_, _, msg) => msg.topic == sample_topic_0
      case _ => false
    })(bs4)
  
    //assert publish arrived to client 3
    assertPacketPending(sample_id_2, {
      case Publish(_, _, msg) => msg.topic == sample_topic_0
      case _ => false
    })(bs4)
  }
  

}
