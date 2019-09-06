package mqtt.utils

import mqtt.utils.BitImplicits._
import org.scalatest.FunSuite

class MqttStringTest extends FunSuite {
  Map[String, Seq[Char]](
    "" -> Seq(0x00, 0x00),
    "☺" -> Seq(0x00, 0x03, 0xE2, 0x98, 0xBA),
    "ciao" -> Seq(0x00, 0x04, 0x63, 0x69, 0x61, 0x6F),
    (0 until 255).map(_ => 'a').mkString -> (Seq[Char](0x00, 0xFF) ++ (0 until 255).map(_ => 'a')),
    (0 until 256).map(_ => 'b').mkString -> (Seq[Char](0x01, 0x00) ++ (0 until 256).map(_ => 'b')),
    (0 until 65535).map(_ => 'c').mkString -> (Seq[Char](0xFF, 0xFF) ++ (0 until 65535).map(_ => 'c'))
  ) foreach {
    case (value, encoded) => {
      val encodedString = encoded.map(_.toByte).toBitsSeq.toBinaryString
      val encodedStr = encodedString.take(40) + (if (encodedString.length > 40) "..." else "")
      val valueStr = value.take(40) + (if (value.length > 40) "..." else "")
      test(s"$valueStr should be encoded in $encodedStr") {
        val data = MqttString.encode(value)
        assert(encoded.map(_.toByte) == data)
      }
    }
  }
}
