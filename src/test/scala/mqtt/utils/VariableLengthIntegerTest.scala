import mqtt.utils.VariableLengthInteger
import org.scalatest.FunSuite
import mqtt.utils.BitImplicits._

class VariableLengthIntegerTest extends FunSuite {
  
  Map[Int, Seq[Char]](
    0 -> Seq(0x00),
    1 -> Seq(0x01),
    10 -> Seq(0x0A),
    127 -> Seq(0x7F),
    128 -> Seq(0x80, 0x01),
    16383 -> Seq(0xFF, 0x7F),
    16384 -> Seq(0x80, 0x80, 0x01),
    2097151 -> Seq(0xFF, 0xFF, 0x7F),
    2097152 -> Seq(0x80, 0x80, 0x80, 0x01),
    268435455 -> Seq(0xFF, 0xFF, 0xFF, 0x7F)
  ) foreach {
    case (value, encoded) => {
      val encodedString = encoded.map(_.toByte).toBitsSeq.toBinaryString
      test(s"$value should be encoded in $encodedString") {
        val data = VariableLengthInteger.encode(value)
        assert(data.length == encoded.length)
        assert(encoded.map(_.toByte) == data)
      }
    }
  }
}
