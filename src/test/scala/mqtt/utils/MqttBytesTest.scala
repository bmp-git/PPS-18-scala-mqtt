package mqtt.utils

import mqtt.utils.BitImplicits._
import org.scalatest.FunSuite

class MqttBytesTest extends FunSuite {
  Map[Seq[Byte], Seq[Char]](
    Seq[Byte]() -> Seq(0x00, 0x00),
    Seq[Byte](0x01, 0x02) -> Seq(0x00, 0x02, 0x01, 0x02),
  ) foreach {
    case (value, encoded) => {
      val encodedString = encoded.map(_.toByte).toBitsSeq.toBinaryString
      test(s"$value should be encoded in $encodedString") {
        val data = MqttBytes.encode(value)
        assert(encoded.map(_.toByte) == data)
      }
    }
  }
}
