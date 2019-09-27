package mqtt.broker

import mqtt.broker.SampleInstances._
import mqtt.broker.state.{Channel, State}
import mqtt.model.Packet.Publish
import org.scalatest.FunSuite

class TestPublish(PublishPacketHandler: (State, Publish, Channel) => State) extends FunSuite {
  
  test("A publish should be forwarded to a client with a matching subscription") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_0.copy(channel = Option(sample_channel_0)))
    val bs2 = bs1.setSession(sample_id_1, sample_session_1.copy(channel = Option(sample_channel_1)))
    val bs3 = PublishPacketHandler(bs2, sample_publish_packet_0, sample_channel_1)
    bs3.sessionFromClientID(sample_id_0).fold(fail)(sess => assert(sess.pendingTransmission.contains(sample_publish_packet_0)))
  }
}
