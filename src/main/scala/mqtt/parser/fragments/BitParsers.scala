package mqtt.parser.fragments

import mqtt.parser.Monad._
import mqtt.parser.Parsers.{Parser, conditional, timesN}
import mqtt.utils.Bit
import mqtt.utils.BitImplicits._
import mqtt.utils.RichOption._
/**
 * A container of bit/byte parsers.
 */
object BitParsers {
  def bit(): Parser[Bit] = Parser(s => on(s.nonEmpty) {(s.head, s.drop(1))})
  
  def bit(which: Bit): Parser[Bit] = conditional(bit())(_ == which)
  
  def zero(): Parser[Bit] = bit(0)
  
  def one(): Parser[Bit] = bit(1)
  
  def bits(count: Int): Parser[Seq[Bit]] = for {bits <- timesN(bit())(count)} yield bits
  
  def byte(): Parser[Byte] = for {bits <- bits(8)} yield bits.toBytes.head
  
  def byte(byte: Byte): Parser[Byte] = conditional(this.byte())(p => p == byte)
  
  def bytes(count: Int): Parser[Seq[Byte]] = for {bytes <- timesN(byte())(count)} yield bytes
  
}
