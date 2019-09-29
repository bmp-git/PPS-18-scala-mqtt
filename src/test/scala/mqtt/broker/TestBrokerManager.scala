package mqtt.broker

import mqtt.broker.Common.updateLastContact
import mqtt.broker.SampleInstances._
import mqtt.broker.state.{Channel, State}
import mqtt.model.ErrorPacket.MalformedPacket
import mqtt.model.Packet
import org.scalatest.FunSuite

import scala.concurrent.duration._


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
  
  test("After 1.5 time KeepAlive without messages should disconnect.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_0.copy(channel = Option(sample_channel_0), keepAlive = 1 seconds))
    val bs2 = updateLastContact(sample_channel_0)(bs1)
    Thread.sleep((4 seconds).toMillis)
    val bs3 = BrokerManager.tick(bs2)
    assert(bs3.closing.contains(sample_channel_0))
  }
  
  test("Sending a publish should update last contact, resetting KeepAlive disconnect checks.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_0.copy(channel = Option(sample_channel_0), keepAlive = 3 seconds))
    val bs2 = updateLastContact(sample_channel_0)(bs1)
    Thread.sleep((3 seconds).toMillis)
    val bs3 = BrokerManager.handle(bs2, sample_publish_packet_1, sample_channel_0)
    Thread.sleep((3 seconds).toMillis)
    val bs4 = BrokerManager.tick(bs3)
    assert(!bs4.closing.contains(sample_channel_0))
  }
}
