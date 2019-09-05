package mqtt.parser

import mqtt.model.QoS
import mqtt.parser.Parsers.MqttParser._
import org.scalatest.{FunSuite, Matchers}
import mqtt.utils.BitImplicits._

class FragmentParserTest extends FunSuite with Matchers {
  //Qos parser
  test("A qos parser should parse a qos") {
    assert(qos().run(Seq(0, 0)) == List((QoS(0), Seq())))
    assert(qos().run(Seq(0, 1)) == List((QoS(1), Seq())))
    assert(qos().run(Seq(1, 0)) == List((QoS(2), Seq())))
  }
  test("A qos parser should not parse a qos3 [MQTT-3.1.2-14]") {
    assert(qos().run(Seq(1, 1)) == List())
  }
  
  //WillFlags parser
  test("A WillFlags parser should parse") {
    assert(willFlags().run(Seq(1, 1, 0, 1)) == List((Option(WillFlags(true, QoS(2))), Seq())))
    assert(willFlags().run(Seq(0, 0, 0, 1)) == List((Option(WillFlags(false, QoS(0))), Seq())))
  }
  test("A WillFlags parser should parse empty flags [no will]") {
    assert(willFlags().run(Seq(0, 0, 0, 0)) == List((Option.empty, Seq())))
  }
  test("A WillFlags parser should not parse if will flag is 0 and will retain is 1 [MQTT-3.1.2-15]") {
    assert(willFlags().run(Seq(1, 0, 0, 0)) == List())
  }
  test("A WillFlags parser should not parse if will flag is 0 and qos is > 0 [MQTT-3.1.2-13]") {
    assert(willFlags().run(Seq(0, 0, 1, 0)) == List())
    assert(willFlags().run(Seq(0, 1, 0, 0)) == List())
  }
}