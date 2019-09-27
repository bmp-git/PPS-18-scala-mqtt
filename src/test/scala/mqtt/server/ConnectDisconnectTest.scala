package mqtt.server

import mqtt.client.DummyClient
import mqtt.model.ErrorPacket.ChannelClosed
import mqtt.model.Packet
import mqtt.model.Packet.ConnectReturnCode.ConnectionAccepted
import mqtt.model.Packet.{Connack, Connect, Disconnect}
import org.scalatest.FunSuite

import scala.concurrent.duration._

class ConnectDisconnectTest extends FunSuite {
  
  def assertSendReceive(actions: Seq[(Duration, Packet)], expected: Seq[Packet]): Unit = {
    val stopper = MqttBroker(9999).run()
    val client = DummyClient(9999, actions)
    client.start()
    val receivedPackets = client.inputPacketStream().toList.toBlocking.single.filter {
      case _: ChannelClosed => false
      case _ => true
    }
    client.join()
    stopper.stop()
    
    
    assert(receivedPackets == expected)
    receivedPackets match {
      case head :: _ => assert(head == Connack(sessionPresent = false, ConnectionAccepted))
    }
  }
  
  test("A client should be able to connect") {
    val connect = Connect(mqtt.model.Packet.Protocol("MQTT", 4), cleanSession = true, 10 second, "Dummyclient", Option.empty, Option.empty)
    val disconnect = Disconnect()
    assertSendReceive(Seq((1 second, connect), (0 second, disconnect)), Seq(Connack(sessionPresent = false, ConnectionAccepted)))
  }
}
