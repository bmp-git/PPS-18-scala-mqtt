package mqtt.utils

import java.nio.charset.StandardCharsets
import BitImplicits._

/**
 * An MQTT strings encoder/decoder
 * In MQTT 3.1.1 strings are encoded in UT8 prefixed by two bytes indicating the length in bytes of the string
 */
object MqttUFT8 {
  /**
   * Get the size of a string
   * @param bytes the buffer with the size in the first two bytes
   * @return the length of the following UTF8 string
   */
  def size(bytes: Seq[Byte]): Int = BigInt(bytes.take(2).toArray).toInt
  
  /**
   * Decode a MQTT string.
   * @param bytes the buffer with the size in the first two bytes and the UTF8 encoded string next
   * @return the string decoded
   */
  def decode(bytes: Seq[Byte]): String = {
    new String(bytes.slice(2, size(bytes) + 2) toArray, StandardCharsets.UTF_8)
  }
  
  /**
   * Encode a MQTT string.
   * @param string the string to encode
   * @return the buffer with the size in the first two bytes and the UTF8 encoded string next
   */
  def encode(string: String): Seq[Byte] = {
    val encoded = string.getBytes(StandardCharsets.UTF_8)
    encoded.size.bits.drop(16).toBytes ++ encoded
  }
}