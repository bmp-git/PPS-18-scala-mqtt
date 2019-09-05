package mqtt.utils

import mqtt.utils.BitImplicits._

object MqttString {
  def encode(value: String): Seq[Byte] = {
    val data = value.getBytes("UTF8").toSeq
    data.length.bits.drop(16).toBytes ++ data
  }
  
  def decode(data: Seq[Byte]): Option[String] = ???
}
