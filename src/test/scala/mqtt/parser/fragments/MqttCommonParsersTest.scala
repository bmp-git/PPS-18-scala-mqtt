package mqtt.parser.fragments
import mqtt.model.Packet.{ApplicationMessage, Credential}
import mqtt.model.QoS
import mqtt.parser.ParserUtils._
import mqtt.parser.datastructure.{CredentialFlags, WillFlags}
import mqtt.parser.fragments.MqttCommonParsers._
import mqtt.utils.BitImplicits._
import mqtt.utils.{Bit, MqttString}
import org.scalatest.{FunSuite, Matchers}

class MqttCommonParsersTest extends FunSuite with Matchers {
  //Qos parser
  test("A QoS parser SHOULD parse a QoS 0") {
    qos() run Seq(0, 0) shouldBe result(QoS(0))
  }
  test("A QoS parser SHOULD parse a QoS 1") {
    qos() run Seq(0, 1) shouldBe result(QoS(1))
  }
  test("A QoS parser SHOULD parse a QoS 2") {
    qos() run Seq(1, 0) shouldBe result(QoS(2))
  }
  test("A QoS parser SHOULD NOT parse a QoS 3 [MQTT-3.1.2-14]") {
    qos() run Seq(1, 1) shouldBe failed
  }
  
  //mqttString parser
  test("A mqttString parser SHOULD parse \"String\"") {
    val value = "String"
    mqttString() run MqttString.encode(value).toBitsSeq shouldBe result(value)
  }
  test("A mqttString parser SHOULD parse empty string") {
    mqttString() run MqttString.encode("").toBitsSeq shouldBe result("")
  }
  test("A mqttString parser SHOULD NOT parse nothing") {
    mqttString() run Seq() shouldBe failed
  }
  
  //mqttInt
  test("A mqttInt parser SHOULD parse 5 in two bytes") {
    mqttInt() run (zerobyte ++ 5.toByte.bits) shouldBe result(5)
  }
  test("A mqttInt parser SHOULD NOT parse a byte") {
    mqttInt() run zerobyte shouldBe failed
  }
  
  //binaryData
  test("A binaryData parser SHOULD parse data with a byte length"){
    val payloadLength = zerobyte ++ 1.toByte.bits
    val payload: Seq[Bit] = byte
    binaryData() run (payloadLength ++ payload) shouldBe result(payload.toBytes)
  }
  test("A binaryData parser SHOULD NOT parse nothing"){
    binaryData() run Seq() shouldBe failed
  }
}
