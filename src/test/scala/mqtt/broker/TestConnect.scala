package mqtt.broker

import mqtt.broker.SampleInstances._
import mqtt.broker.UtilityFunctions._
import mqtt.broker.state.{Channel, State}
import mqtt.model.Packet.ConnectReturnCode._
import mqtt.model.Packet._
import mqtt.model.QoS
import org.scalatest.FunSuite

import scala.concurrent.duration.Duration

trait TestConnect extends FunSuite {
  def ConnectHandler: (State, Connect, Channel) => State
  
  test("Sending a connect packet with an invalid will message topic should disconnect.") {
    val packet = sample_connect_packet_0.copy(protocol = Protocol("HTTP", 4))
    val bs1 = ConnectHandler(bs0, packet, sample_channel_0)
    assertClosing(sample_channel_0)(bs1)
  }
  
  test("Sending a connect packet with and unsupported protocol name should disconnect.") {
    val packet = sample_connect_packet_0.copy(willMessage = Option(sample_application_message_0.copy(topic = "#")))
    val bs1 = ConnectHandler(bs0, packet, sample_channel_0)
    assertClosing(sample_channel_0)(bs1)
  }
  
  
  def checkDisconnectionWithConnACKAfterConnect(packet: Connect, returnCode: ConnectReturnCode): Unit = {
    val bs1 = ConnectHandler(bs0, packet, sample_channel_0)
    assertClosingWithPacket(sample_channel_0, {
      case Connack(_, `returnCode`) => true
      case _ => false
    })(bs1)
  }
  
  
  test("Sending a connect packet with and unsupported protocol version should disconnect.") {
    val packet = sample_connect_packet_0.copy(protocol = Protocol("MQTT", 3))
    checkDisconnectionWithConnACKAfterConnect(packet, UnacceptableProtocolVersion)
  }
  
  test("Sending a connect packet with a bad client identifier should disconnect.") {
    val packet = sample_connect_packet_0.copy(clientId = "")
    checkDisconnectionWithConnACKAfterConnect(packet, IdentifierRejected)
  }
  
  test("Sending a connect packet without user and password to a server with anonymous false should disconnect.") {
    val bs1 = ConnectHandler(bs0_auth, sample_connect_packet_0, sample_channel_0)
    assertNotAuthorized(sample_channel_0)(bs1)
  }
  
  test("Sending a connect packet with a bad password should disconnect.") {
    val bs1 = ConnectHandler(bs0_auth, sample_connect_packet_1.copy(credential = sample_credential_1), sample_channel_0)
    assertNotAuthorized(sample_channel_0)(bs1)
  }
  
  test("Sending a connect packet with a bad username should disconnect.") {
    val bs1 = ConnectHandler(bs0_auth, sample_connect_packet_1.copy(credential = sample_credential_2), sample_channel_0)
    assertNotAuthorized(sample_channel_0)(bs1)
  }
  
  test("Sending a connect packet with the right user and password should respond with ack 0.") {
    val bs1 = ConnectHandler(bs0_auth, sample_connect_packet_1, sample_channel_0)
    assertConnectionAccepted(sample_id_0)(bs1)
  }
  
  test("Sending a connect packet with the right username and empty password should respond with ack 0 if on the server the stored password is also empty.") {
    val bs1 = ConnectHandler(bs0_auth, sample_connect_packet_1.copy(credential = sample_credential_3), sample_channel_0)
    assertConnectionAccepted(sample_id_0)(bs1)
  }
  
  test("Sending a connect packet with user and password on a server with anonymous true should respond with ack 0.") {
    val bs1 = ConnectHandler(bs0, sample_connect_packet_1, sample_channel_0)
    assertConnectionAccepted(sample_id_0)(bs1)
  }
  
  test("Sending a legit connect packet should respond with ack 0.") {
    val bs1 = ConnectHandler(bs0, sample_connect_packet_0, sample_channel_0)
    assertConnectionAccepted(sample_id_0)(bs1)
  }
  
  test("Sending a connect packet with CleanSession 0 should clear the session.") {
    val packet = sample_connect_packet_0.copy(cleanSession = true)
    val bs1 = bs0.setSession(sample_id_0, sample_session_0)
    val bs2 = ConnectHandler(bs1, packet, sample_channel_0)
    bs2.sessionFromClientID(sample_id_0).fold(fail)(s => {
      assert(s.subscriptions.isEmpty)
    })
  }
  
  def checkPersistentFlag(packet: Connect, persistent: Boolean): Unit = {
    val bs1 = bs0.setSession(sample_id_0, sample_session_0)
    val bs2 = ConnectHandler(bs1, packet, sample_channel_0)
    bs2.sessionFromClientID(sample_id_0).fold[Unit](fail)(s => {
      assert(s.persistent == persistent)
    })
  }
  
  test("Sending a connect with cleanSession 1 should set the persistent session flag to 0.") {
    val packet = sample_connect_packet_0.copy(cleanSession = true)
    checkPersistentFlag(packet, persistent = false)
  }
  
  test("Sending a connect with cleanSession 0 should set the persistent session flag to 1.") {
    val packet = sample_connect_packet_0.copy(cleanSession = false)
    checkPersistentFlag(packet, persistent = true)
  }
  
  def checkCleanSession(cleanSession: Boolean, SessionPresent: Boolean, SessionWasPresent: Boolean): Unit = {
    val packet = sample_connect_packet_0.copy(cleanSession = cleanSession)
    val bs1 = if (SessionWasPresent) bs0.setSession(sample_id_0, sample_session_0) else bs0
    val bs2 = ConnectHandler(bs1, packet, sample_channel_0)
    bs2.sessionFromClientID(sample_id_0).fold[Unit](fail)(s => {
      assert(s.pendingTransmission.exists { case Connack(SessionPresent, ConnectionAccepted) => true })
    })
  }
  
  test("Sending a connect packet with CleanSession 1 should reply with Session Present 0.") {
    checkCleanSession(cleanSession = true, SessionPresent = false, SessionWasPresent = true)
  }
  
  test("Sending a connect packet with CleanSession 0 should reply with Session Present 1 if session was present.") {
    checkCleanSession(cleanSession = false, SessionPresent = true, SessionWasPresent = true)
  }
  
  test("Sending a connect packet with CleanSession 0 should reply with Session Present 0 if session was not present.") {
    checkCleanSession(cleanSession = false, SessionPresent = false, SessionWasPresent = false)
  }
  
  test("A Connect from a new channel should disconnect the old one.") {
    val bs1 = ConnectHandler(bs0, sample_connect_packet_0, sample_channel_0)
    val bs2 = ConnectHandler(bs1, sample_connect_packet_0, sample_channel_1)
    assert(bs2.closing.get(sample_channel_0).isDefined)
  }
  
  test("A Connect from a new channel should replace the old one.") {
    val bs1 = ConnectHandler(bs0, sample_connect_packet_0, sample_channel_0)
    val bs2 = ConnectHandler(bs1, sample_connect_packet_0, sample_channel_1)
    bs2.sessionFromClientID(sample_id_0).fold(fail)(s => {
      s.channel.fold(fail)(sk => assert(sk == sample_channel_1))
    })
  }
  
  test("The will message should be saved.") {
    val packet = sample_connect_packet_0.copy(willMessage = Option(sample_application_message_0))
    val bs1 = ConnectHandler(bs0, packet, sample_channel_0)
    bs1.wills.get(sample_channel_0).fold(fail)(m => assert(m.topic == sample_topic_0))
  }
  
  test("The keep alive should be saved.") {
    val packet = sample_connect_packet_0.copy(keepAlive = Duration(10, "minutes"))
    val bs1 = ConnectHandler(bs0, packet, sample_channel_0)
    bs1.sessionFromClientID(sample_id_0).fold(fail)(s => {
      assert(s.keepAlive == Duration(10, "minutes"))
    })
  }
  
  test("Connecting two times should disconnect.") {
    val bs1 = ConnectHandler(bs0, sample_connect_packet_0, sample_channel_0)
    val bs2 = ConnectHandler(bs1, sample_connect_packet_0, sample_channel_0)
    assertClosing(sample_channel_0)(bs2)
  }
  
  test("Causing a disconnection should publish the will message.") {
    val applicationMessage = ApplicationMessage(retain = false, QoS(0), sample_topic_0, Seq())
    val packet = sample_connect_packet_0.copy(clientId = sample_id_1, willMessage = Option(applicationMessage))
  
    val bs1 = bs0.setSession(sample_id_0, sample_session_0.copy(channel = Option(sample_channel_0))) //he is subscribed to topic0
    val bs2 = ConnectHandler(bs1, packet, sample_channel_1) //sets will message
    val bs3 = ConnectHandler(bs2, packet, sample_channel_1) //will be disconnected and will published
  
    assertPacketPending(sample_id_0, {
      case p: Publish => p.message == applicationMessage
      case _ => false
    })(bs3)
  }
}
