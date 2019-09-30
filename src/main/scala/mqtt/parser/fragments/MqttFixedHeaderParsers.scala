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
 * A container of MQTT 3.1.1 fixed header elements.
 */
object MqttFixedHeaderParsers {
  def packetType(mask: PacketMask): Parser[Seq[Bit]] = for {
    a <- bit(mask code 0)
    b <- bit(mask code 1)
    c <- bit(mask code 2)
    d <- bit(mask code 3)
  } yield Seq(a, b, c, d)
  
  def reserved(): Parser[Seq[Bit]] = timesN(zero())(4)
  
  def reserved2(): Parser[Seq[Bit]] = seqN(zero(), zero(), one(), zero())
  
  def variableLength(): Parser[Int] = Parser(s => {
    VariableLengthInteger.decode(s.toBytes) match {
      case (length, bytes) => length.fold[Option[(Int, Seq[Bit])]](Option.empty)(l =>
        on(bytes.size == l) {
          (l, bytes.toSeq.toBitsSeq)
        })
    }
  })
  
  def publishFlags(): Parser[PublishFlags] = for {
    dup <- bit()
    qos <- qos(); _ <- fail(!dup && qos != QoS(0))
    retain <- bit()
  } yield PublishFlags(dup, qos, retain)
  
}
