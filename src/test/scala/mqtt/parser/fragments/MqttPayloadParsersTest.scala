package mqtt.parser.fragments

import mqtt.model.Packet.{ApplicationMessage, Credential}
import mqtt.model.QoS
import mqtt.parser.ParserUtils._
import mqtt.parser.datastructure.{CredentialFlags, WillFlags}
import mqtt.parser.fragments.MqttPayloadParsers._
import mqtt.utils.BitImplicits._
import mqtt.utils.{Bit, MqttString}
import org.scalatest.{FunSuite, Matchers}

class MqttPayloadParsersTest extends FunSuite with Matchers {
  //willPayload
  test("A will payload parser SHOULD parse ApplicationMessage(retain = false, QoS(0), \"a/b\", payload))") {
    val retain = false
    val qos = QoS(0)
    val payloadLength = zerobyte ++ 1.toByte.bits
    val payload: Seq[Bit] = byte
    val topic = "a/b"
    willPayload(Option(WillFlags(retain, qos)))
      .run(MqttString.encode(topic).toBitsSeq ++ payloadLength ++ payload)
      .shouldBe(result(Option(ApplicationMessage(retain, qos, topic, payload.toBytes))))
  }
  test("A will payload parser SHOULD parse nothing") {
    willPayload(Option.empty) run Seq() shouldBe result(Option.empty)
  }
  
  //credentials
  test("A credentials parser SHOULD parse username and password") {
    val username = "username"
    val passwordLength = zerobyte ++ 1.toByte.bits
    val password: Seq[Bit] = byte
    credentials(CredentialFlags(username = true, password = true))
      .run(MqttString.encode(username).toBitsSeq ++ passwordLength ++ password)
      .shouldBe(result(Option(Credential(username, Option(password.toBytes)))))
  }
  test("A credentials parser SHOULD parse only username") {
    val username = "username"
    credentials(CredentialFlags(username = true, password = false))
      .run(MqttString.encode(username).toBitsSeq)
      .shouldBe(result(Option(Credential(username, Option.empty))))
  }
  test("A credentials parser SHOULD parse nothing") {
    credentials(CredentialFlags(username = false, password = false)) run Seq() shouldBe result(Option.empty)
  }
  
  //subscriptionGrantedQoS
  test("A subscriptionGrantedQoS parser SHOULD parse Seq(0, 0, 0, 0, 0, 0, 0, 0) with QoS 0") {
    subscriptionGrantedQoS() run zerobyte shouldBe result(Option(QoS(0)))
  }
  test("A subscriptionGrantedQoS parser SHOULD parse Seq(1, 0, 0, 0, 0, 0, 0, 0) with empty") {
    subscriptionGrantedQoS() run Seq(1, 0, 0, 0, 0, 0, 0, 0) shouldBe result(Option.empty)
  }
  test("A subscriptionGrantedQoS parser SHOULD NOT parse Seq(0, 1, 0, 0, 0, 0, 0, 0) with empty") {
    subscriptionGrantedQoS() run Seq(0, 1, 0, 0, 0, 0, 0, 0) shouldBe failed
  }
  
  //subscription
  test("A subscription parser SHOULD parse \"a/b\" with QoS 0") {
    val topic = "a/b"
    val qos = QoS(0)
    subscription() run (MqttString.encode(topic).toBitsSeq ++ zerobyte) shouldBe result((topic, qos))
  }
}
