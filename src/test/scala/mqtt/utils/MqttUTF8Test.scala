package mqtt.utils

import org.scalatest.{FunSuite, Matchers}

class MqttUTF8Test extends FunSuite with Matchers {
  
  test("An MqttUTF-8 encoder should encode and decode MQTT string") {
    val mqtt = "MQTT"
    val encoded: Seq[Byte] = MqttUFT8.encode(mqtt)
    mqtt shouldBe MqttUFT8.decode(encoded)
  }
}
