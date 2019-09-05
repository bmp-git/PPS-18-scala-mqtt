package mqtt.utils

import java.nio.charset.StandardCharsets
import BitImplicits._

object MqttUFT8 {
  def size(bytes: Seq[Byte]): Int = BigInt(bytes.take(2).toArray).toInt
  
  def decode(bytes: Seq[Byte]): String = {
    new String(bytes.slice(2, size(bytes) + 2) toArray, StandardCharsets.UTF_8)
  }
  
  def encode(string: String): Seq[Byte] = {
    val encoded = string.getBytes(StandardCharsets.UTF_8)
    encoded.size.bits.drop(16).toBytes ++ encoded
  }
}