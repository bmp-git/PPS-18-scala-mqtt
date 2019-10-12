package mqtt.broker

import mqtt.broker.Common.updateLastContact
import mqtt.broker.SampleInstances._
import mqtt.broker.UtilityFunctions._
import mqtt.broker.state.{Channel, State}
import mqtt.model.ErrorPacket.MalformedPacket
import mqtt.model.Packet
import mqtt.model.Packet.{Pingresp, Publish}
import org.scalatest.FunSuite

import scala.concurrent.duration._


class TestBrokerManager extends FunSuite with TestConnect with TestDisconnect with TestSubscribe with TestPublish with TestUnsubscribe with TestPingReq {
  override def ConnectHandler: (State, Packet.Connect, Channel) => State = BrokerManager.handle
  
  override def DisconnectHandler: (State, Packet.Disconnect, Channel) => State = BrokerManager.handle
  
  override def SubscribeHandler: (State, Packet.Subscribe, Channel) => State = BrokerManager.handle
  
  override def PublishHandler: (State, Packet.Publish, Channel) => State = BrokerManager.handle
  
  override def UnsubscribeHandler: (State, Packet.Unsubscribe, Channel) => State = BrokerManager.handle
  
  override def PingReqHandler: (State, Packet.Pingreq, Channel) => State = BrokerManager.handle
  
  test("Receiving a malformed packet should disconnect the client.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_0.copy(channel = Option(sample_channel_0)))
    val bs2 = BrokerManager.handle(bs1, MalformedPacket(), sample_channel_0)
    assert(bs2.closing.contains(sample_channel_0))
  }
  
  test("An unsupported packet should not change the state of the server.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_0.copy(channel = Option(sample_channel_0)))
    val bs2 = BrokerManager.handle(bs1, Pingresp(), sample_channel_0)
    assert(bs2 == bs1)
  }
  
  test("After disconnection new packets should be discarded.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_0.copy(channel = Option(sample_channel_0)))
    val bs2 = bs1.setSession(sample_id_1, sample_session_1.copy(channel = Option(sample_channel_1)))
    
    val bs3 = BrokerManager.handle(bs2, MalformedPacket(), sample_channel_1) //disconnect
    
    val bs4 = PublishHandler(bs3, sample_publish_packet_0, sample_channel_1)
    
    assertPacketNotPending(sample_id_0, {
      case _: Publish => true
      case _ => false
    })(bs4)
  }
  
  test("After 1.5 time KeepAlive without messages should disconnect.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_0.copy(channel = Option(sample_channel_0), keepAlive = 1 seconds))
    val bs2 = updateLastContact(sample_channel_0)(bs1)
    Thread.sleep((4 seconds).toMillis)
    val bs3 = BrokerManager.tick(bs2)
    assertClosing(sample_channel_0)(bs3)
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
