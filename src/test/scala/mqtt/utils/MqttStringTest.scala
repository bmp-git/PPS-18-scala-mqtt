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
      test(s"$value should be encoded in $encodedString") {
        val data = MqttString.encode(value)
        assert(encoded.map(_.toByte) == data)
      }
    }
  }
}
