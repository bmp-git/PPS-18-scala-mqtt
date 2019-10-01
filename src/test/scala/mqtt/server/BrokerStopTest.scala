package mqtt.server

import mqtt.model.BrokerConfig
import org.scalatest.FunSuite

class BrokerStopTest extends FunSuite {
  test("A broker should shutdown gracefully and free the listening port") {
    MqttBroker(BrokerConfig(port = 50004), Map()).run().stop()
    Thread.sleep(1000) //give time to the OS to release the port
    MqttBroker(BrokerConfig(port = 50004), Map()).run().stop()
    Thread.sleep(1000) //give time to the OS to release the port
    MqttBroker(BrokerConfig(port = 50004), Map()).run().stop()
  }
  
  test("A broker not started shouldn't bind the port") {
    MqttBroker(BrokerConfig(port = 50005), Map())
    val mqtt = MqttBroker(BrokerConfig(port = 50005), Map())
    MqttBroker(BrokerConfig(port = 50005), Map())
    mqtt.run().stop()
  }
}
