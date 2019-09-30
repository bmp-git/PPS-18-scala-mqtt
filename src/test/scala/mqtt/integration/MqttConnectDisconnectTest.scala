package mqtt.integration

import mqtt.server.MqttBroker
import org.eclipse.paho.client.mqttv3.{MqttClient, MqttConnectOptions}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite, Matchers}

/**
 * Test of connection and disconnection to/from the broker using Paho MQTT client library.
 */
class MqttConnectDisconnectTest extends FunSuite with BeforeAndAfterAll with BeforeAndAfterEach with Matchers {
  val serverPort = 10001
  def opt(cleanSession: Boolean): MqttConnectOptions = {
    val opt = new MqttConnectOptions()
    opt.setCleanSession(cleanSession)
    opt.setMqttVersion(4) //MQTT 3.1.1
    opt
  }
  
  test("As a client, I want to connect and disconnect from the MQTTbroker"){
    val server = MqttBroker(serverPort).run()
    val client = new MqttClient(s"tcp://localhost:$serverPort", MqttClient.generateClientId())
    
    client.connect(opt(true))
    assert(client isConnected)
    
    client.disconnect()
    assert(!client.isConnected)
    
    client.close()
    server.stop()
  }
  
  test("As a client, I should want to keep my session when reconnecting"){
    val server = MqttBroker(serverPort).run()
    val client = new MqttClient(s"tcp://localhost:$serverPort", MqttClient.generateClientId())
    
    val conn = client.connectWithResult(opt(false))
    assert(!conn.getSessionPresent)
    client.disconnect()
    
    val conn2 = client.connectWithResult(opt(false))
    assert(conn2 getSessionPresent)
    client.disconnect()
    
    client.close()
    server.stop()
  }
}
