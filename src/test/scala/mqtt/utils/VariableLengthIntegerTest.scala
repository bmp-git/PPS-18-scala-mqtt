package mqtt.utils

import mqtt.utils.BitImplicits._
import org.scalatest.FunSuite

class VariableLengthIntegerTest extends FunSuite {
  Seq[(Option[Int], Seq[Char])](
    Option(0) -> Seq(0x00),
    Option(1) -> Seq(0x01),
    Option(10) -> Seq(0x0A),
    Option(127) -> Seq(0x7F),
    Option(128) -> Seq(0x80, 0x01),
    Option(16383) -> Seq(0xFF, 0x7F),
    Option(16384) -> Seq(0x80, 0x80, 0x01),
    Option(2097151) -> Seq(0xFF, 0xFF, 0x7F),
    Option(2097152) -> Seq(0x80, 0x80, 0x80, 0x01),
    Option(268435455) -> Seq(0xFF, 0xFF, 0xFF, 0x7F),
    Option.empty -> Seq(),
    Option.empty -> Seq(0xFF, 0xFF, 0xFF, 0xFF, 0x7F),
  ) foreach {
    case (value, encoded) => {
      val encodedString = encoded.map(_.toByte).toBitsSeq.toBinaryString
  
      if (value.isDefined) {
        test(s"$value should be encoded in $encodedString") {
          val data = VariableLengthInteger.encode(value.fold(0)(n => n))
          assert(encoded.map(_.toByte) == data)
        }
      }
  
      test(s"$encodedString should be decoded in $value") {
        val decoded = VariableLengthInteger.decode(encoded.map(_.toByte).toStream)
        assert(decoded._1 == value)
        assert(decoded._2.isEmpty)
      }
    }
  }
}
