package mqtt.parser

import Monad._
import mqtt.parser.Parsers.{Parser, conditional}
import mqtt.utils.Bit
import mqtt.utils.BitImplicits._

object BitParsers {
  // a parser of any single bit , failing on empty lists [Nil or List()?]
  def item(): Parser[Bit] = Parser(s => if (s.isEmpty) List() else List((s.head, s.tail)))
  
  // gets symbol bit
  def bit(bit: Bit): Parser[Bit] = conditional(item())(_ == bit)
  
  def byte(byte: Byte): Parser[Byte] = Parser(bits =>
    if (bits.size < 8 || bits.take(8).toBytes.head != byte) List() else List((bits.take(8).toBytes.head, bits.drop(8))))
  
  def zero(): Parser[Bit] = bit(0)
  
  def one(): Parser[Bit] = bit(1)
  
  //[Nil or List()?]
  def bits(count: Int): Parser[Seq[Bit]] = Parser(bits => if(bits.size < count) List() else List((bits.take(count), bits.drop(count))))
  
  def bytes(count: Int): Parser[Seq[Byte]] = for { bytes <- bits(count * 8) } yield bytes.toBytes
}
