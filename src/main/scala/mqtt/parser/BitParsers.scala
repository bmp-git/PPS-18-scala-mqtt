package mqtt.parser

import mqtt.parser.Monad._
import mqtt.parser.Parsers.{Parser, conditional, timesN, assure}
import mqtt.utils.Bit
import mqtt.utils.BitImplicits._

/**
 * A container of bit/byte parsers.
 */
object BitParsers {
  def bit(): Parser[Bit] = Parser(s => if (s.isEmpty) Option.empty else Option((s.head, s.drop(1))))
    //Parser(s => s.headOption.fold[Option[(Bit, Seq[Bit])]](Option.empty)(h => Option((h, s.tail))))
  
  def bit(which: Bit): Parser[Bit] = conditional(bit())(_ == which)
  
  def zero(): Parser[Bit] = bit(0)
  
  def one(): Parser[Bit] = bit(1)
  
  def bits(count: Int): Parser[Seq[Bit]] = for { bits <- timesN(bit())(count) } yield bits
  
  def byte(): Parser[Byte] = for { bits <- timesN(bit())(8)} yield bits.toBytes.head
  
  def byte(byte: Byte): Parser[Byte] = for { parsed <- this.byte(); _ <- assure(parsed == byte)} yield parsed
  
  def bytes(count: Int): Parser[Seq[Byte]] = for {bytes <- bits(count * 8)} yield bytes.toBytes
  
}
