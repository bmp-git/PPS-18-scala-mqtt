package mqtt.parser

import mqtt.parser.BitParsers.bit
import mqtt.parser.Parsers.{or, ifConditionFails}
import ParserUtils._
import org.scalatest.{FunSuite, Matchers}

class ParsersTest extends FunSuite with Matchers {
  //defaultIfNot parser
  test("A DefaultIfNot parser SHOULD parse and consume input if condition=true") {
    ifConditionFails(zero, bit())(condition = true) run Seq(one, one) shouldBe partialResult(one)(Seq(one))
  }
  test("A DefaultIfNot parser SHOULD NOT parse and consume input if condition=false") {
    ifConditionFails(zero, bit())(condition = false) run Seq(zero) shouldBe partialResult(zero)(Seq(zero))
  }
  
  //or parser
  test("An Exclusive Or parser SHOULD parse and consume input if one parser is used") {
    or(BitParsers.zero(), BitParsers.one(), BitParsers.one()) run Seq(zero) shouldBe result(zero)
  }
  test("An Exclusive Or parser SHOULD parse and consume input and return one value if two parser are used (the first matching)") {
    or(BitParsers.zero(),  BitParsers.one(), Parsers.success("asd")) run Seq(one) shouldBe result(one)
  }
}