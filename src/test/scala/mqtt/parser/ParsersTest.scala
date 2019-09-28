package mqtt.parser

import mqtt.parser.Parsers.{first, skip, many, many1, optional, seqN}
import ParserUtils._
import mqtt.parser.fragments.BitParsers
import org.scalatest.{FunSuite, Matchers}

class ParsersTest extends FunSuite with Matchers {
  //skip parser
  test("An skip parser SHOULD parse and consume input if condition=false") {
    skip(BitParsers.bit())(condition = false, zero) run Seq(one, one) shouldBe partialResult(one)(Seq(one))
  }
  test("An skip parser SHOULD NOT parse and consume input if condition=true") {
    skip(BitParsers.bit())(condition = true, zero) run Seq(zero) shouldBe partialResult(zero)(Seq(zero))
  }
  
  //first parser
  test("A first parser SHOULD parse and consume input if one parser is used") {
    first(BitParsers.zero(), BitParsers.one(), BitParsers.one()) run Seq(zero) shouldBe result(zero)
  }
  test("A first parser SHOULD parse and consume input and return the first successful parsing result") {
    first(BitParsers.zero(),  BitParsers.one(), Parsers.success("asd")) run Seq(one) shouldBe result(one)
  }
  test("A first parser SHOULD NOT parse if any parser succeeded") {
    first(BitParsers.zero(),  BitParsers.zero()) run Seq(one) shouldBe failed
  }
  
  //seqN parser
  test("A seqN parser SHOULD parse using more parsers") {
    seqN(BitParsers.zero(),  BitParsers.one()) run Seq(zero, one) shouldBe result(List(zero, one))
  }
  
  test("A seqN parser SHOULD NOT parse using more parsers if one fails") {
    seqN(BitParsers.zero(),  BitParsers.one()) run Seq(one, one) shouldBe failed
  }

  //optional
  test("An optional parser SHOULD parse and consume input if the parser success") {
    optional(empty, BitParsers.zero()) run Seq(zero) shouldBe result(zero)
  }
  
  test("An optional parser SHOULD return default if the parser fails without consuming input") {
    val default = zero
    optional(default, BitParsers.zero()) run Seq(one) shouldBe partialResult(default)(Seq(one))
  }
  
  //many
  test("A many parser SHOULD parse one element") {
    many1(BitParsers.zero()) run Seq(zero) shouldBe result(List(zero))
  }
  test("A many parser SHOULD parse many elements") {
    many(BitParsers.zero()) run Seq(zero, zero, zero) shouldBe result(List(zero, zero, zero))
  }
  test("A many parser SHOULD parse nothing without consuming input") {
    many(BitParsers.zero()) run Seq(one, zero) shouldBe partialResult(List())(Seq(one, zero))
  }
  
  //many1
  test("A many1 parser SHOULD parse one element") {
    many1(BitParsers.zero()) run Seq(zero) shouldBe result(List(zero))
  }
  test("A many1 parser SHOULD parse many elements") {
    many1(BitParsers.zero()) run Seq(zero, zero, one) shouldBe partialResult(List(zero, zero))(Seq(one))
  }
  test("A many1 parser SHOULD NOT parse nothing") {
    many1(BitParsers.one()) run Seq(zero, zero, zero) shouldBe failed
  }
}