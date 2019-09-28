package mqtt.broker

import mqtt.broker.SampleInstances._
import mqtt.broker.state.{Channel, State}
import mqtt.model.ErrorPacket.MalformedPacket
import mqtt.model.Packet
import org.scalatest.FunSuite

class TestBrokerManager extends FunSuite with TestConnect with TestDisconnect with TestSubscribe with TestPublish {
  override def ConnectHandler: (State, Packet.Connect, Channel) => State = BrokerManager.handle
  
  override def DisconnectHandler: (State, Packet.Disconnect, Channel) => State = BrokerManager.handle
  
  override def SubscribeHandler: (State, Packet.Subscribe, Channel) => State = BrokerManager.handle
  
  override def PublishHandler: (State, Packet.Publish, Channel) => State = BrokerManager.handle
  
  test("Receiving a malformed packet should disconnect the client") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_0.copy(channel = Option(sample_channel_0)))
    val bs2 = BrokerManager.handle(bs1, MalformedPacket(), sample_channel_0)
    assert(bs2.closing.contains(sample_channel_0))
  }
}
