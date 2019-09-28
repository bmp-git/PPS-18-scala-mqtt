package mqtt.parser.fragments

import mqtt.parser.ParserUtils._
import mqtt.utils.BitImplicits._
import org.scalatest.{FunSuite, Matchers}
import mqtt.parser.fragments.BitParsers.{bit, bits, bytes}

class BitParsersTest extends FunSuite with Matchers {
  //Bit parser
  test("A Bit parser SHOULD parse a single zero bit") {
    bit() run Seq(zero) shouldBe result(zero)
  }
  test("A Bit parser SHOULD parse a single one bit") {
    bit() run Seq(one) shouldBe result(one)
  }
  test("A Bit parser SHOULD parse the first bit of a sequence and return the remaining") {
    bit() run some shouldBe partialResult(some head)(some drop 1)
  }
  test("A Bit parser SHOULD NOT parse an empty sequence") {
    bit() run Seq() shouldBe failed
  }
  
  //Bit(which) parser
  test("A Bit(0) parser SHOULD parse a single specific zero bit") {
    bit(zero) run Seq(zero) shouldBe result(zero)
  }
  test("A Bit(1) parser SHOULD parse a single specific one bit") {
    bit(one) run Seq(one) shouldBe result(one)
  }
  test("A Bit(0) parser SHOULD parse a single specific zero bit in a sequence and return the remaining") {
    bit(zero) run some shouldBe partialResult(zero)(some drop 1)
  }
  test("A Bit(0) parser SHOULD NOT parse a different bit") {
    bit(zero) run Seq(one) shouldBe failed
  }
  
  //Zero parser
  test("A Zero parser SHOULD parse a zero bit in a sequence and return the remaining") {
   BitParsers.zero() run some shouldBe partialResult(zero)(some drop 1)
  }
  test("A Zero parser SHOULD NOT parse a one") {
    BitParsers.zero() run Seq(one) shouldBe failed
  }
  
  //One parser
  test("A One parser SHOULD parse a one bit") {
    BitParsers.one() run Seq(one) shouldBe result(one)
  }
  test("A One parser SHOULD NOT parse something starting with a zero bit") {
    BitParsers.one() run some shouldBe failed
  }
  
  //Bits parser
  test("A Bits parser SHOULD parse exactly n bits") {
    bits(some size) run some shouldBe result(some)
  }
  test("A Bits parser SHOULD parse first n bits of a n+1 bit sequence and return the last") {
    bits(some.size - 1) run some shouldBe partialResult(some take(some.size - 1))(Seq(some last))
  }
  test("A Bits parser SHOULD NOT parse n bits if the sequence is less than n bits") {
    bits(some.size + 1) run some shouldBe failed
  }
  test("A Bits parser SHOULD NOT parse and empty sequence") {
    bits(some size) run Seq() shouldBe failed
  }
  
  //Bytes parser
  test("A Byte parser SHOULD parse a byte") {
    bytes(1) run byte shouldBe result(byte toBytes)
  }
  test("A Byte parser SHOULD NOT parse a byte if sequence is less then a byte") {
    bytes(1) run Seq() shouldBe failed
  }
}