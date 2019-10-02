package mqtt.broker

import mqtt.broker.SampleInstances._
import mqtt.broker.UtilityFunctions._
import mqtt.broker.state.{Channel, State}
import mqtt.model.Packet.{Publish, Suback, Subscribe}
import mqtt.model.{QoS, Topic, TopicFilter}
import org.scalatest.FunSuite

trait TestSubscribe extends FunSuite {
  def SubscribeHandler: (State, Subscribe, Channel) => State
  
  test("Sending a subscribe without first having connected should disconnect.") {
    val bs1 = SubscribeHandler(bs0, sample_subscribe_packet_0, sample_channel_0)
    assertClosing(sample_channel_0)(bs1)
  }
  
  test("A subscribe packet with a legit filter should reply with a suback with a success return code.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_1.copy(channel = Option(sample_channel_0)))
    val bs2 = SubscribeHandler(bs1, sample_subscribe_packet_0, sample_channel_0)
    assertPacketPending(sample_id_0, { case Suback(id, subs) if id == sample_packet_id_0 => subs.contains(Some(QoS(0))) })(bs2)
  }
  
  test("A subscribe packet with a non legit filter should reply with a suback with an error return code.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_1.copy(channel = Option(sample_channel_0)))
    val bs2 = SubscribeHandler(bs1, sample_subscribe_packet_0.copy(topics = Seq(("#sport/", QoS(0)))), sample_channel_0)
    assertPacketPending(sample_id_0, { case Suback(id, subs) if id == sample_packet_id_0 => subs.contains(None) })(bs2)
  }
  
  test("A subscribe with an empty filter list should disconnect.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_1.copy(channel = Option(sample_channel_0)))
    val bs2 = SubscribeHandler(bs1, sample_subscribe_packet_0.copy(topics = Seq()), sample_channel_0)
    assert(bs2.sessionFromClientID(sample_id_0).isEmpty)
  }
  
  
  test("After a subscription the retain message should be published, with retain flag true.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_1.copy(channel = Option(sample_channel_0)))
    val bs2 = bs1.setRetainMessage((new Topic(sample_topic_0)), sample_application_message_0)
    val bs3 = SubscribeHandler(bs2, sample_subscribe_packet_0.copy(topics = Seq(((sample_topic_0), QoS(0)))), sample_channel_0)
    assertPacketPending(sample_id_0, {
      case Publish(dup, _, msg) if !dup && msg.retain => msg.payload == sample_application_message_0.payload && msg.topic == sample_application_message_0.topic
      case _ => false
    })(bs3)
  }
  
  test("After a subscription to an already subscribed topic the retain message should be published, with retain flag true.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_0.copy(channel = Option(sample_channel_0))) //client 1 subscribed to sport/tennis
    
    val bs2 = bs1.setRetainMessage((new Topic(sample_topic_0)), sample_application_message_0) //set retain message for sport/tennis
    
    val bs3 = SubscribeHandler(bs2, sample_subscribe_packet_0, sample_channel_0) // client 1 resubscribes to sport/tennis
    
    //client 1 should receive the retain message
    assertPacketPending(sample_id_0, {
      case Publish(_, _, msg) if msg.retain => msg.payload == sample_application_message_0.payload && msg.topic == sample_application_message_0.topic
      case _ => false
    })(bs3)
  }
  
  test("After a subscription to an already subscribed topic should update the QoS.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_0
      .copy(channel = Option(sample_channel_0), subscriptions = Map(new TopicFilter(sample_topic_0) -> QoS(2)))) //client 1 subscribed to sport/tennis with QoS2
    
    val bs2 = SubscribeHandler(bs1, sample_subscribe_packet_0, sample_channel_0) // client 1 resubscribes to sport/tennis with QoS0
  
    bs2.sessionFromClientID(sample_id_0).fold[Unit](fail)(s => {
      s.subscriptions.get(new TopicFilter(sample_topic_0)).fold[Unit](fail) { case QoS(v) => assert(v == 0) }
    })
  }
  
  test("After a subscription the retain message should be published only to the subscriber, with retain flag true.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_0.copy(channel = Option(sample_channel_0))) //client 1 subscribed to sport/tennis
    val bs2 = bs1.setSession(sample_id_1, sample_session_1.copy(channel = Option(sample_channel_1))) //client 2 empty subscriptions
    
    val bs3 = bs2.setRetainMessage((new Topic(sample_topic_0)), sample_application_message_0) //set retain message for sport/tennis
    
    //client 2 subscribes to sport/tennis
    val bs4 = SubscribeHandler(bs3, sample_subscribe_packet_0, sample_channel_1)
    
    //client 2 should receive the retain message with retain flag true
    assertPacketPending(sample_id_1, {
      case Publish(_, _, msg) if msg.retain => msg.payload == sample_application_message_0.payload && msg.topic == sample_application_message_0.topic
      case _ => false
    })(bs4)
    
    //client 1 should not receive the retain message
    assertPacketNotPending(sample_id_0, {
      case _: Publish => true
      case _ => false
    })(bs4)
  }
  
  test("Return codes in ack are in the same order.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_1.copy(channel = Option(sample_channel_0)))
    val bs2 = SubscribeHandler(bs1, sample_subscribe_packet_0.copy(topics = Seq(("#sport/", QoS(0)), ("/++/", QoS(0)), ("sport/#", QoS(0)))), sample_channel_0)
    assertPacketPending(sample_id_0, { case Suback(id, Seq(None, None, Some(_))) if id == sample_packet_id_0 => true })(bs2)
  }
  
  test("After a subscription with multiple overlapping filters should receive multiple retain publish.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_1.copy(channel = Option(sample_channel_0)))
    val bs2 = bs1.setRetainMessage(new Topic(sample_topic_0), sample_application_message_0)
    val bs3 = bs2.setRetainMessage(new Topic(sample_topic_1), sample_application_message_1)
    val bs4 = SubscribeHandler(bs3, sample_subscribe_packet_0.copy(topics = Seq(("#", QoS(0)), ("/#", QoS(0)))), sample_channel_0)
    val pubCount = bs4.sessionFromClientID(sample_id_0).fold(fail)(s => s.pendingTransmission.count {
      case _: Publish => true
      case _ => false
    })
    
    assert(pubCount == 4)
  }
}
