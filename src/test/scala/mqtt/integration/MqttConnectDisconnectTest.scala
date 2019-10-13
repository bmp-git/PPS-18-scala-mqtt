package mqtt.integration

import mqtt.model.BrokerConfig
import mqtt.server.MqttBroker
import org.eclipse.paho.client.mqttv3.{MqttClient, MqttConnectOptions}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite, Matchers}

/**
 * Test of connection and disconnection to/from the broker using Paho MQTT client library.
 */
class MqttConnectDisconnectTest extends FunSuite with BeforeAndAfterAll with BeforeAndAfterEach with Matchers {
  def opt(cleanSession: Boolean): MqttConnectOptions = {
    val opt = new MqttConnectOptions()
    opt.setCleanSession(cleanSession)
    opt.setMqttVersion(4) //MQTT 3.1.1
    opt
  }
  
  test("As a client, I want to connect and disconnect from the MQTTbroker"){
    val server = MqttBroker(BrokerConfig(port = 50100), Map()).run()
    val client = new MqttClient(s"tcp://localhost:50100", MqttClient.generateClientId())
    
    client.connect(opt(true))
    assert(client isConnected)
    
    client.disconnect()
    assert(!client.isConnected)
    
    client.close()
    server.stop()
  }
  
  test("As a client, I should want to keep my session when reconnecting"){
    val server = MqttBroker(BrokerConfig(port = 50101), Map()).run()
    val client = new MqttClient(s"tcp://localhost:50101", MqttClient.generateClientId())
    
    val conn = client.connectWithResult(opt(false))
    assert(!conn.getSessionPresent)
    client.disconnect(2000)

    val conn2 = client.connectWithResult(opt(false))
    assert(conn2 getSessionPresent)
    client.disconnect(2000)
    
    client.close()
    server.stop()
  }
}
