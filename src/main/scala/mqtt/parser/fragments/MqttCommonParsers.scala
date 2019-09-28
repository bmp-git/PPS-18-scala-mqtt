package mqtt.parser.fragments

import mqtt.model.QoS
import mqtt.parser.Monad._
import mqtt.parser.Parsers.{Parser, fail, skip}
import mqtt.parser.fragments.BitParsers._
import mqtt.utils.BitImplicits._
import mqtt.utils.MqttString
import mqtt.utils.RichOption._

/**
 * A container of MQTT 3.1.1 common elements.
 */
object MqttCommonParsers {
  def qos(): Parser[QoS] = for {
    most <- bit()
    least <- bit(); _ <- fail(most && least)
  } yield QoS(BigInt(Seq(most, least).toBytes toArray) intValue)
  
  
  def utf8(): Parser[String] = Parser(s =>
    on(s.size > 2) {
      (MqttString.decode(s.toBytes), s.toBytes.drop(MqttString.size(s.toBytes) + 2).toBitsSeq)
    })
  
  def binaryData(): Parser[Seq[Byte]] = for {
    length <- twoBytesInt()
    payload <- skip(bytes(length))(length == 0, Seq())
  } yield payload
  
  def twoBytesInt(): Parser[Int] = for {bytes <- bytes(2)} yield BigInt(bytes toArray) intValue
}
