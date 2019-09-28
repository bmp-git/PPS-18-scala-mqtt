package mqtt.server

import mqtt.client.DummyClient
import mqtt.model.Packet
import mqtt.model.Packet.ConnectReturnCode.ConnectionAccepted
import mqtt.model.Packet.{Connack, Connect, Disconnect}
import org.scalatest.FunSuite

import scala.concurrent.duration._

class ConnectDisconnectTest extends FunSuite {
  
  def assertSendReceive(data: Seq[(Seq[Packet], Seq[Packet])]): Unit = {
    val stopper = MqttBroker(9999).run()
    
    data map {
      case (actions, expected) =>
        val client = DummyClient(9999, actions)
        client.start()
        (client, expected)
    } foreach {
      case (client, expected) =>
        client.join()
        val received = client.receivedPackets()
        assert(received == expected)
    }
    
    stopper.stop()
  }
  
  def basicConnect(clientId: String): Connect =
    Connect(mqtt.model.Packet.Protocol("MQTT", 4), cleanSession = true, 10 second, clientId, Option.empty, Option.empty)
  
  test("A client should be able to connect") {
    val connect1 = basicConnect("c1")
    val connect2 = basicConnect("c2")
    val disconnect = Disconnect()
    val connack = Connack(sessionPresent = false, ConnectionAccepted)
    assertSendReceive(Seq[(Seq[Packet], Seq[Packet])](
      (Seq(connect1, disconnect), Seq(connack)),
      (Seq(connect2, disconnect), Seq(connack))
    ))
  }
}
