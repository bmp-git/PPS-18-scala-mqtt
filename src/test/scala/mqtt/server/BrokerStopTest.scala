package mqtt.server

import org.scalatest.FunSuite

class BrokerStopTest extends FunSuite {
  test("A broker should shutdown gracefully and free the listening port") {
    MqttBroker(50004).run().stop()
    MqttBroker(50004).run().stop()
    MqttBroker(50004).run().stop()
  }
  
  test("A broker not started shouldn't bind the port") {
    MqttBroker(50005)
    val mqtt = MqttBroker(50005)
    MqttBroker(50005)
    mqtt.run().stop()
  }
}
