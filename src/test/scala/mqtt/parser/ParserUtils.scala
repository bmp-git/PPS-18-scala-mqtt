package mqtt.parser

import mqtt.utils.Bit
import mqtt.utils.BitImplicits._

object ParserUtils {
  val zero: Bit = Bit(false)
  val one: Bit = Bit(true)
  val some: Seq[Bit] = Seq(0, 1, 0, 1, 1)
  val byte: Seq[Bit] = Seq(1, 1, 1, 1, 1, 1, 1, 1)
  val zerobyte: Seq[Bit] = Seq(0, 0, 0, 0, 0, 0, 0, 0)
  
  val failed = Option.empty
  def result[A](value: A): Option[(A, Seq[Bit])] = Option((value, Seq()))
  def partialResult[A](value: A)(remaining: Seq[Bit]): Option[(A, Seq[Bit])] = Option((value, remaining))
}
