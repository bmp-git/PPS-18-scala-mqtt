package mqtt.broker

import mqtt.broker.SampleInstances._
import mqtt.model.ErrorPacket.MalformedPacket
import org.scalatest.FunSuite

class TestBrokerManager extends FunSuite {
  test("Receiving a malformed packet should disconnect the client") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_0.copy(channel = Option(sample_channel_0)))
    val bs2 = BrokerManager.handle(bs1, MalformedPacket(), sample_channel_0)
    assert(bs2.closing.contains(sample_channel_0))
  }
}
