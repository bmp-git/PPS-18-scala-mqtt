package mqtt.server

import mqtt.client.DummyClient
import mqtt.model.Packet
import mqtt.model.Packet.Connect

import scala.concurrent.duration._

object AssertionsHelper {
  
  def assertSendReceive(port: Int)(data: Seq[(Seq[Packet], Seq[Packet])]): Unit = {
    val breaker = MqttBroker(port).run()
    
    data map {
      case (actions, expected) =>
        val client = DummyClient(port, actions)
        client.start()
        (client, expected)
    } foreach {
      case (client, expected) =>
        client.join()
        val received = client.receivedPackets()
        assert(received == expected)
    }
  
    breaker.stop()
  }
  
  def basicConnect(clientId: String): Connect =
    Connect(mqtt.model.Packet.Protocol("MQTT", 4), cleanSession = true, 10 second, clientId, Option.empty, Option.empty)
}
