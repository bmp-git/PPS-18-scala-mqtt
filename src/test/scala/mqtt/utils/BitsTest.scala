package mqtt.utils

import mqtt.utils.BitImplicits._
import org.scalatest.FunSuite

class BitsTest extends FunSuite {
  
  test("0 should be implicitly be a 0 bit") {
    val bit: Bit = 0
    assert(!bit)
  }
  test("1 should be implicitly be a 1 bit") {
    val bit: Bit = 1
    assert(bit)
  }
  test("false should be implicitly be a 0 bit") {
    val bit: Bit = false
    assert(!bit)
  }
  test("true should be implicitly be a 1 bit") {
    val bit: Bit = true
    assert(bit)
  }
  test("2 should be implicitly be a 1 bit") {
    val bit: Bit = BitImplicits.intToBit(2)
    assert(bit)
  }
  test("-1 should be implicitly be a 1 bit") {
    val bit: Bit = BitImplicits.intToBit(2)
    assert(bit)
  }
  
  Map[Seq[Byte], Seq[Bit]](
    Seq[Byte]() -> Seq[Bit](),
    Seq[Byte](-1) -> Seq[Bit](1, 1, 1, 1, 1, 1, 1, 1),
    Seq[Byte](127, 0, 1) -> Seq[Bit](0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
  ) foreach {
    case (value, encoded) => {
      test(s"$value should be converted in $value") {
        val data: Seq[Bit] = value.toBitsSeq
        assert(encoded == data)
      }
    }
  }
  
  Map[(Seq[Bit], Int, Int), BigInt](
    (Seq[Bit](0), 0, 1) -> 0,
    (Seq[Bit](1), 0, 1) -> 1,
    (Seq[Bit](1, 0), 0, 2) -> 2,
    (Seq[Bit](1, 0, 0, 0, 0, 0, 0, 0), 0, 8) -> 128,
    (Seq[Bit](1, 0, 0, 0, 0, 0, 0, 0, 0), 0, 9) -> 256,
    (Seq[Bit](1, 0, 0, 0, 0, 0, 0, 0, 0, 0), 0, 10) -> 512,
    (Seq[Bit](1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0), 1, 9) -> 257,
    (Seq[Bit](1, 0, 0, 0, 0, 0, 0, 0, 0), 0, 2) -> 2,
    (Seq[Bit](1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0), 1, 9) -> 129,
  ) foreach {
    case (key, result) => key match {
      case (bits: Seq[Bit], from: Int, to: Int) => {
        val str = bits.toBinaryString
        test(s"$str from $from and taking $to bits should be converted in $result") {
          val res = bits.getValue(from, to)
          assert(res == result)
        }
      }
    }
  }
  
  test("Bitify of [1] should be 00000001") {
    val bits = new Bitify(Seq[Byte](1))
    assert(bits(7) == Bit(true))
    assert(bits.toBinaryString == "00000001")
  }
}
