package mqtt.parser.fragments

import mqtt.model.QoS
import mqtt.parser.Monad._
import mqtt.parser.Parsers.{Parser, fail, seqN, timesN}
import mqtt.parser.datastructure.{PacketMask, PublishFlags}
import mqtt.parser.fragments.BitParsers._
import mqtt.parser.fragments.MqttCommonParsers.qos
import mqtt.utils.BitImplicits._
import mqtt.utils.RichOption._
import mqtt.utils.{Bit, VariableLengthInteger}

/**
 * Contains parsers of MQTT 3.1.1 fixed header elements.
 */
object MqttFixedHeaderParsers {
  /**
   * A MQTT packet type parser that parse only the requested packet.
   * In MQTT the 14 packet types are encoded using 4 bits.
   *
   * @param mask the packet mask
   * @return the parser
   */
  def packetType(mask: PacketMask): Parser[Seq[Bit]] = for {
    a <- bit(mask code 0)
    b <- bit(mask code 1)
    c <- bit(mask code 2)
    d <- bit(mask code 3)
  } yield Seq(a, b, c, d)
  
  /**
   * A parser of 4 zeros (0,0,0,0).
   * In every MQTT packet except Publish, Pubrel, Subscribe and Unsubscribe (0,0,1,0) is used to complete the first byte.
   *
   * @return the parser
   */
  def reserved(): Parser[Seq[Bit]] = timesN(zero())(4)
  
  /**
   * A parser of (0,0,1,0).
   * In MQTT Pubrel, Subscribe and Unsubscribe packets (0,0,1,0) is used to complete the first byte.
   *
   * @return the parser
   */
  def reserved2(): Parser[Seq[Bit]] = seqN(zero(), zero(), one(), zero())
  
  /**
   * A MQTT publish flags parsers.
   * The parser produce the publish flags (duplicate, qos, retain).
   *
   * @return the parser
   */
  def publishFlags(): Parser[PublishFlags] = for {
    dup <- bit()
    qos <- qos(); _ <- fail(dup && qos == QoS(0))
    retain <- bit()
  } yield PublishFlags(dup, qos, retain)
  
  /**
   * A MQTT variable length parser. The parser fails if the remaining input length is different.
   * The parser produce the length of the remaining packet (variable-header + payload).
   *
   * @return the parser
   */
  def variableLength(): Parser[Int] = Parser(s => {
    VariableLengthInteger.decode(s.toBytes) match {
      case (length, bytes) => length.fold[Option[(Int, Seq[Bit])]](Option.empty)(l =>
        on(bytes.size == l) {
          (l, bytes.toSeq.toBitsSeq)
        })
    }
  })
}
