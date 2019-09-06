package mqtt.parser

import org.scalatest.{FunSuite, Matchers}
import mqtt.parser.BitParsers._
import mqtt.utils.Bit
import mqtt.utils.BitImplicits._

class BitParsersTest extends FunSuite with Matchers {
  val zero: Bit = Bit(false)
  val one: Bit = Bit(true)
  val some: Seq[Bit] = Seq(0, 1, 0, 1, 1)
  val byte: Seq[Bit] = Seq(1, 1, 1, 1, 1, 1, 1, 1)
  
  //Item parser
  test("An item parser SHOULD parse a single zero bit") {
    assert(item().run(Seq(zero)) == List((zero, Seq())))
  }
  test("An item parser SHOULD parse a single one bit") {
    assert(item().run(Seq(one)) == List((one, Seq())))
  }
  test("An item parser SHOULD parse the first bit of a sequence and return the remaining") {
    assert(item().run(some) == List((some.head, some.tail)))
  }
  test("A item parser SHOULD NOT parse an empty sequence") {
    assert(item().run(Seq()) == List())
  }
  
  //Bit parser
  test("A Bit parser SHOULD parse a single specific zero bit") {
    assert(bit(zero).run(Seq(zero)) == List((zero, Seq())))
  }
  test("A Bit parser SHOULD parse a single specific one bit") {
    assert(bit(one).run(Seq(one)) == List((one, Seq())))
  }
  test("A Bit parser SHOULD parse a single specific zero bit in a sequence and return the remaining") {
    assert(bit(zero).run(some) == List((zero, some.tail)))
  }
  test("A Bit parser SHOULD NOT parse a different bit") {
    assert(bit(zero).run(Seq(one)) == List())
  }
  
  //Zero parser
  test("A Zero parser SHOULD parse a zero bit in a sequence and return the remaining") {
    assert(BitParsers.zero().run(some) == List((zero, some.tail)))
  }
  test("A Zero parser SHOULD NOT parse a one") {
    assert(BitParsers.zero().run(Seq(one)) == List())
  }
  
  //One parser
  test("A One parser SHOULD parse a one bit") {
    assert(BitParsers.one().run(Seq(one)) == List((one, Seq())))
  }
  test("A One parser SHOULD NOT parse something starting with a zero bit") {
    assert(BitParsers.one().run(some) == List())
  }
  
  //Bits parser
  test("A Bits parser SHOULD parse exactly n bits") {
    assert(bits(some size).run(some) == List((some, Seq())))
  }
  test("A Bits parser SHOULD parse first n bits of a n+1 bit sequence and return the last") {
    assert(bits(some.size - 1).run(some) == List((some.take(some.size - 1), Seq(some.last))))
  }
  test("A Bits parser SHOULD NOT parse n bits if the sequence is less than n bits") {
    assert(bits(some.size + 1).run(some) == List())
  }
  test("A Bits parser SHOULD NOT parse and empty sequence") {
    assert(bits(some size).run(Seq()) == List())
  }
  
  //Bytes parser
  test("A Byte parser SHOULD parse a byte") {
    assert(bytes(1).run(byte) == List((byte.toBytes, Seq())))
  }
  test("A Byte parser SHOULD NOT parse a byte if sequence is less then a byte") {
    assert(bytes(1).run(Seq()) == List())
  }
  
  //defaultIfNot parser
  /*test("A DefaultIfNot parser SHOULD parse and consume input if condition=true") {
    assert(ifConditionFails(zero, item())(true).run(Seq(1,1)) == List((one, Seq(one))))
  }
  test("A DefaultIfNot parser SHOULD NOT parse and consume input if condition=false") {
    assert(ifConditionFails(zero, item())(false).run(Seq(0)) == List((zero, Seq(zero))))
  }*/
}