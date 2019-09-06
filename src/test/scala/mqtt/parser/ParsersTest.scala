package mqtt.parser

import org.scalatest.{FunSuite, Matchers}
import mqtt.parser.Parsers._
import mqtt.parser.BitParsers._
import mqtt.utils.Bit
import mqtt.utils.BitImplicits._

class ParsersTest extends FunSuite with Matchers {
  val zero: Bit = Bit(false)
  val one: Bit = Bit(true)
  
  //defaultIfNot parser
  test("A DefaultIfNot parser SHOULD parse and consume input if condition=true") {
    assert(ifConditionFails(zero, item())(true).run(Seq(1,1)) == List((one, Seq(one))))
  }
  test("A DefaultIfNot parser SHOULD NOT parse and consume input if condition=false") {
    assert(ifConditionFails(zero, item())(false).run(Seq(0)) == List((zero, Seq(zero))))
  }
}