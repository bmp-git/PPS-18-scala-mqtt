package mqtt.utils

case class Bit(value: Boolean) {
  override def toString: String = if (value) "1" else "0"
}