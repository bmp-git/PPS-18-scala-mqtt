package mqtt.server

import org.scalatest.FunSuite

class BrokerStopTest extends FunSuite {
  test("A broker should shutdown gracefully and free the listening port") {
    MqttBroker(50002).run().stop()
    MqttBroker(50002).run().stop()
    MqttBroker(50002).run().stop()
  }
  
  test("A broker not started shouldn't bind the port") {
    MqttBroker(50002)
    val mqtt = MqttBroker(50002)
    MqttBroker(50002)
    mqtt.run().stop()
  }
}
