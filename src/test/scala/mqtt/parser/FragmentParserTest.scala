package mqtt.parser

import mqtt.model.QoS
import mqtt.parser.Parsers.MqttParser._
import org.scalatest.{FunSuite, Matchers}
import mqtt.utils.BitImplicits._

class FragmentParserTest extends FunSuite with Matchers {
  //Qos parser
  test("A QoS parser SHOULD parse a QoS 0") {
    assert(qos().run(Seq(0, 0)) == List((QoS(0), Seq())))
  }
  test("A QoS parser SHOULD parse a QoS 1") {
    assert(qos().run(Seq(0, 1)) == List((QoS(1), Seq())))
  }
  test("A QoS parser SHOULD parse a QoS 2") {
    assert(qos().run(Seq(1, 0)) == List((QoS(2), Seq())))
  }
  test("A QoS parser SHOULD NOT parse a QoS 3 [MQTT-3.1.2-14]") {
    assert(qos().run(Seq(1, 1)) == List())
  }
  
  //WillFlags parser
  test("A WillFlags parser SHOULD parse flags when will_flag=1, will_retain=1, will_QoS=2") {
    assert(willFlags().run(Seq(1, 1, 0, 1)) == List((Option(WillFlags(true, QoS(2))), Seq())))
  }
  test("A WillFlags parser SHOULD parse flags when will_flag=1, will_retain=0, will_QoS=0") {
    assert(willFlags().run(Seq(0, 0, 0, 1)) == List((Option(WillFlags(false, QoS(0))), Seq())))
  }
  test("A WillFlags parser SHOULD parse empty flags [will_flag=1, will_retain=0, will_QoS=0]") {
    assert(willFlags().run(Seq(0, 0, 0, 0)) == List((Option.empty, Seq())))
  }
  test("A WillFlags parser SHOULD NOT parse if will_flag=0, will_retain=1 [MQTT-3.1.2-15]") {
    assert(willFlags().run(Seq(1, 0, 0, 0)) == List())
  }
  test("A WillFlags parser SHOULD NOT parse if will_flag=0, QoS=1 [MQTT-3.1.2-13]") {
    assert(willFlags().run(Seq(0, 0, 1, 0)) == List())
  }
  test("A WillFlags parser SHOULD NOT parse if will_flag=0, QoS=2 [MQTT-3.1.2-13]") {
    assert(willFlags().run(Seq(0, 1, 0, 0)) == List())
  }
}