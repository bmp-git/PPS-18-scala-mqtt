package mqtt.parser.fragments

import mqtt.model.Packet.ConnectReturnCode._
import mqtt.model.QoS
import mqtt.parser.ParserUtils.{failed, result, zerobyte}
import mqtt.parser.datastructure.WillFlags
import mqtt.parser.fragments.MqttVariableHeaderParsers._
import mqtt.utils.BitImplicits._
import org.scalatest.{FunSuite, Matchers}

class MqttVariableHeaderParsersTest extends FunSuite with Matchers {
  //WillFlags parser
  test("A WillFlags parser SHOULD parse flags when will_flag=1, will_retain=1, will_QoS=2") {
    willFlags() run Seq(1, 1, 0, 1) shouldBe result(Option(WillFlags(retain = true, QoS(2))))
  }
  test("A WillFlags parser SHOULD parse flags when will_flag=1, will_retain=0, will_QoS=0") {
    willFlags() run Seq(0, 0, 0, 1) shouldBe result(Option(WillFlags(retain = false, QoS(0))))
  }
  test("A WillFlags parser SHOULD parse empty flags [will_flag=1, will_retain=0, will_QoS=0]") {
    willFlags() run Seq(0, 0, 0, 0) shouldBe result(Option.empty)
  }
  test("A WillFlags parser SHOULD NOT parse if will_flag=0, will_retain=1 [MQTT-3.1.2-15]") {
    willFlags() run Seq(1, 0, 0, 0) shouldBe failed
  }
  test("A WillFlags parser SHOULD NOT parse if will_flag=0, QoS=1 [MQTT-3.1.2-13]") {
    willFlags() run Seq(0, 0, 1, 0) shouldBe failed
  }
  test("A WillFlags parser SHOULD NOT parse if will_flag=0, QoS=2 [MQTT-3.1.2-13]") {
    willFlags() run Seq(0, 1, 0, 0) shouldBe failed
  }
  
  //ConnectReturnCode
  test("A ConnectReturnCode parser SHOULD parse a ConnectionAccepted") {
    connectReturnCode() run(0.toByte.bits) shouldBe result(ConnectionAccepted)
  }
  test("A ConnectReturnCode parser SHOULD parse a UnacceptableProtocolVersion") {
    connectReturnCode() run(1.toByte.bits) shouldBe result(UnacceptableProtocolVersion)
  }
  test("A ConnectReturnCode parser SHOULD parse a IdentifierRejected") {
    connectReturnCode() run(2.toByte.bits) shouldBe result(IdentifierRejected)
  }
  test("A ConnectReturnCode parser SHOULD parse a ServerUnavailable") {
    connectReturnCode() run(3.toByte.bits) shouldBe result(ServerUnavailable)
  }
  test("A ConnectReturnCode parser SHOULD parse a BadUsernameOrPassword") {
    connectReturnCode() run(4.toByte.bits) shouldBe result(BadUsernameOrPassword)
  }
  test("A ConnectReturnCode parser SHOULD parse a NotAuthorized") {
    connectReturnCode() run(5.toByte.bits) shouldBe result(NotAuthorized)
  }
  test("A ConnectReturnCode parser SHOULD NOT parse a byte > 5") {
    connectReturnCode() run(31.toByte.bits) shouldBe failed
  }
  
  //Packet identifier
  test("A PacketIdentifier parser SHOULD NOT parse an id=0 [MQTT-2.3.1-1]") {
    packetIdentifier() run (zerobyte ++ zerobyte) shouldBe failed
  }
}