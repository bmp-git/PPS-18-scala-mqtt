package mqtt.integration

import mqtt.server.MqttBroker
import org.eclipse.paho.client.mqttv3.{MqttClient, MqttConnectOptions}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite, Matchers}

/**
 * An abstract class test that set up and close the MQTT broker and a client when completed.
 * Before and after each test the client connect and disconnect
 *
 * @param serverIp   the mqtt server ip
 * @param serverPort the mqtt server port
 */
abstract class MqttClientServerTest(serverIp: String, serverPort: Int) extends FunSuite with BeforeAndAfterAll with BeforeAndAfterEach with Matchers {
  val defaultOpt: MqttConnectOptions = {
    val opt = new MqttConnectOptions()
    opt.setMqttVersion(4) //MQTT 3.1.1, clean session true
    opt
  }
  
  var server: MqttBroker#Breaker = _
  var client: MqttClient = _
  
  override def beforeAll(): Unit = {
    server = MqttBroker(serverPort).run()
    client = new MqttClient(s"tcp://$serverIp:$serverPort", MqttClient.generateClientId())
  }
  
  override def beforeEach(): Unit = {
    client.connect(defaultOpt)
  }
  
  override def afterAll(): Unit = {
    client.close()
    server.stop()
  }
  
  override def afterEach(): Unit = {
    client.disconnect()
  }
}
