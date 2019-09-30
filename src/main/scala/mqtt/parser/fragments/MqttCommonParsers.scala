package mqtt.parser.fragments

import mqtt.model.QoS
import mqtt.parser.Monad._
import mqtt.parser.Parsers.{Parser, fail, skip}
import mqtt.parser.fragments.BitParsers._
import mqtt.utils.BitImplicits._
import mqtt.utils.MqttString
import mqtt.utils.RichOption._

/**
 * Contains some parsers of MQTT 3.1.1 common elements.
 */
object MqttCommonParsers {
  /**
   * A QoS parser. In MQTT the QoS (Quality of Service) is represented using 2 bits.
   * The parser produce a QoS that can be QoS0, QoS1, QoS2. Fails otherwise.
   *
   * @return the parser
   */
  def qos(): Parser[QoS] = for {
    most <- bit()
    least <- bit(); _ <- fail(most && least)
  } yield QoS(BigInt(Seq(most, least).toBytes toArray) intValue)
  
  
  /**
   * A MQTT string parser. In MQTT strings are encoded in UTF-8 preceded by 2 bytes indicating the size.
   * The parser produce a string.
   *
   * @return the parser
   */
  def mqttString(): Parser[String] = Parser(s =>
    on(s.size >= 2) {
      (MqttString.decode(s.toBytes), s.toBytes.drop(MqttString.size(s.toBytes) + 2).toBitsSeq)
    })
  
  /**
   * A MQTT int parser. In MQTT int are encoded using 2 bytes.
   * The parser produce an int.
   *
   * @return the parser
   */
  def mqttInt(): Parser[Int] = for {bytes <- bytes(2)} yield BigInt(bytes toArray) intValue
  
  /**
   * A parser of binary data preceded by 2 byte length.
   * The parser produce a sequence of bytes.
   *
   * @return the parser
   */
  def binaryData(): Parser[Seq[Byte]] = for {
    length <- mqttInt()
    payload <- skip(bytes(length))(length == 0, Seq())
  } yield payload
}
