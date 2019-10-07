package mqtt.parser.fragments

import mqtt.model.QoS
import mqtt.parser.ParserUtils._
import mqtt.parser.datastructure.PublishFlags
import mqtt.parser.fragments.MqttFixedHeaderParsers._
import mqtt.utils.Bit
import org.scalatest.{FunSuite, Matchers}
import mqtt.utils.BitImplicits._

class MqttFixedHeaderParsersTest extends FunSuite with Matchers {
  //reserved
  test("A reserved parser SHOULD parse (0,0,0,0)") {
    val bits: Seq[Bit] = Seq(0, 0, 0, 0)
    reserved() run bits shouldBe result(bits)
  }
  test("A reserved parser SHOULD NOT parse (0,0,1,0)") {
    reserved() run Seq(0, 0, 0, 1) shouldBe failed
  }
  test("A reserved parser SHOULD NOT parse (0,0)") {
    reserved() run Seq(0, 0) shouldBe failed
  }
  //reserved2
  test("A reserved2 parser SHOULD parse (0,0,1,0)") {
    val bits: Seq[Bit] = Seq(0, 0, 1, 0)
    reserved2() run bits shouldBe result(bits)
  }
  test("A reserved2 parser SHOULD NOT parse (0,0,0,0)") {
    reserved2() run Seq(0, 0, 0, 0) shouldBe failed
  }
  test("A reserved2 parser SHOULD NOT parse (0,0)") {
    reserved2() run Seq(0, 0) shouldBe failed
  }
  
  //publishFlags
  test("A publish flags parser SHOULD parse (0,0,0,1) !dup, qos 0, retain") {
    publishFlags() run Seq(0, 0, 0, 1) shouldBe result(PublishFlags(duplicate = false, QoS(0), retain = true))
  }
  test("A publish flags parser SHOULD parse (1,0,1,1) dup, qos 1, retain") {
    publishFlags() run Seq(1, 0, 1, 1) shouldBe result(PublishFlags(duplicate = true, QoS(1), retain = true))
  }
  test("A publish flags parser SHOULD parse (0,1,0,1) dup, qos 2, retain") {
    publishFlags() run Seq(0, 1, 0, 1) shouldBe result(PublishFlags(duplicate = false, QoS(2), retain = true))
  }
  test("A publish flags parser SHOULD NOT parse (1,0,0,1) dup, qos 0, !retain") {
    publishFlags() run Seq(1, 0, 0, 1) shouldBe failed
  }
  
  //variableLength
  test("A variableLength SHOULD PARSE a zero length") {
    variableLength() run Seq(0) shouldBe result(0)
  }
  test("A variableLength SHOULD PARSE a 3 bytes length followed by exactly 3 bytes") {
    val bytes3 = byte ++ byte ++ byte
    variableLength() run(Seq(0x03).map(_.toByte).toBitsSeq ++ bytes3) shouldBe partialResult(3)(bytes3)
  }
  test("A variableLength SHOULD NOT PARSE a 3 bytes length followed by more bytes (4)") {
    val bytes4 = byte ++ byte ++ byte ++ byte
    variableLength() run (Seq(0x03).map(_.toByte).toBitsSeq ++ bytes4) shouldBe failed
  }
  test("A variableLength SHOULD NOT PARSE a 127 bytes length followed by less bytes (0)") {
    variableLength() run Seq(0x7F).map(_.toByte).toBitsSeq shouldBe failed
  }
  test("A variableLength SHOULD NOT PARSE nothing") {
    variableLength() run Seq() shouldBe failed
  }
}