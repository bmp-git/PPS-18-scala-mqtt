package mqtt.broker

import mqtt.broker.SampleInstances._
import mqtt.broker.UtilityFunctions._
import mqtt.broker.state.{Channel, State}
import mqtt.model.Packet.{Unsuback, Unsubscribe}
import mqtt.model.{QoS, TopicFilter}
import org.scalatest.FunSuite

trait TestUnsubscribe extends FunSuite {
  def UnsubscribeHandler: (State, Unsubscribe, Channel) => State
  
  
  test("An unsubscribe with an empty filter list should disconnect.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_0.copy(channel = Option(sample_channel_0)))
    val bs2 = UnsubscribeHandler(bs1, Unsubscribe(sample_packet_id_0, Seq()), sample_channel_0)
    assert(bs2.sessionFromClientID(sample_id_0).isEmpty)
  }
  
  test("An unsubscribe should respond with an UNSUBACK with the same packet id.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_0.copy(channel = Option(sample_channel_0)))
    val bs2 = UnsubscribeHandler(bs1, sample_unsubscribe_packet_0, sample_channel_0)
    assertPacketPending(sample_id_0, {
      case Unsuback(`sample_packet_id_0`) => true
      case _ => false
    })(bs2)
  }
  
  test("An unsubscribe with a matching subscription should remove it.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_0.copy(channel = Option(sample_channel_0)))
    val bs2 = UnsubscribeHandler(bs1, sample_unsubscribe_packet_0, sample_channel_0)
    bs2.sessionFromClientID(sample_id_0).fold(fail)(s => assert(s.subscriptions.isEmpty))
  }
  
  test("An unsubscribe with a non matching subscription should not remove it.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_0.copy(channel = Option(sample_channel_0)))
    val bs2 = UnsubscribeHandler(bs1, sample_unsubscribe_packet_1, sample_channel_0)
    bs2.sessionFromClientID(sample_id_0).fold(fail)(s => assert(s.subscriptions.nonEmpty))
  }
  
  test("An unsubscribe that does not remove any subscription should still respond with UNSUBACK.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_0.copy(channel = Option(sample_channel_0)))
    val bs2 = UnsubscribeHandler(bs1, sample_unsubscribe_packet_1, sample_channel_0)
    assertPacketPending(sample_id_0, {
      case Unsuback(`sample_packet_id_0`) => true
      case _ => false
    })(bs2)
  }
  
  test("An unsubscribe should only remove the matching subscriptions.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_0.copy(
      channel = Option(sample_channel_0),
      subscriptions = Map(
        new TopicFilter("sport/swimming") -> QoS(0),
        new TopicFilter("games/perudo") -> QoS(0),
        new TopicFilter("$SYS/#") -> QoS(0),
        new TopicFilter("sport/swimming/") -> QoS(0),
        new TopicFilter("sport/+") -> QoS(0)
      )
    ))
    val bs2 = UnsubscribeHandler(bs1, Unsubscribe(sample_packet_id_0, Seq("sport/+", "games/perudo")), sample_channel_0)
    bs2.sessionFromClientID(sample_id_0).fold(fail)(s => {
      assert {
        s.subscriptions.size == 3 &&
          s.subscriptions.contains(new TopicFilter("sport/swimming")) &&
          s.subscriptions.contains(new TopicFilter("$SYS/#")) &&
          s.subscriptions.contains(new TopicFilter("sport/swimming/"))
      }
    })
  }
}
