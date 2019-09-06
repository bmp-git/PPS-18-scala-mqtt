package mqtt.parser

import org.scalatest.{FunSuite, Matchers}
import mqtt.parser.Parsers._
import mqtt.parser.BitParsers._
import mqtt.utils.Bit

class ParsersTest extends FunSuite with Matchers {
  val zero: Bit = Bit(false)
  val one: Bit = Bit(true)
  
  //defaultIfNot parser
  test("A DefaultIfNot parser SHOULD parse and consume input if condition=true") {
    assert(ifConditionFails(zero, bit())(true).run(Seq(one, one)) == List((one, Seq(one))))
  }
  test("A DefaultIfNot parser SHOULD NOT parse and consume input if condition=false") {
    assert(ifConditionFails(zero, bit())(false).run(Seq(zero)) == List((zero, Seq(zero))))
  }
  
  //or parser
  test("An Or parser SHOULD parse and consume input if one parser is used") {
    assert(or(BitParsers.zero(), BitParsers.one(), BitParsers.one()).run(Seq(zero)) == List((zero, Seq())))
  }
  test("An Or parser SHOULD parse and consume input and return more values if two parser are used") {
    assert(or(BitParsers.zero(), BitParsers.one(), BitParsers.one()).run(Seq(one)) == List((one, Seq()),(one, Seq())))
  }
}