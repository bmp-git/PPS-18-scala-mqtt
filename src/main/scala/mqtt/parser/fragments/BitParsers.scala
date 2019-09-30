package mqtt.parser.fragments

import mqtt.parser.Monad._
import mqtt.parser.Parsers.{Parser, conditional, timesN}
import mqtt.utils.Bit
import mqtt.utils.BitImplicits._
import mqtt.utils.RichOption._

/**
 * Contains a set of bit/byte parsers.
 */
object BitParsers {
  /**
   * A single bit parser that fails on empty input. Producing the bit and consuming the input otherwise.
   *
   * @return the parser
   */
  def bit(): Parser[Bit] = Parser(s => on(s.nonEmpty) {
    (s.head, s.drop(1))
  })
  
  /**
   * A single bit parser that fails if the bit is different from `which`.
   *
   * @param which the bit wanted
   * @return the parser
   */
  def bit(which: Bit): Parser[Bit] = conditional(bit())(_ == which)
  
  /**
   * A zero bit parser. Fails if the bit is one.
   *
   * @return the parser
   */
  def zero(): Parser[Bit] = bit(0)
  
  /**
   * A one bit parser. Fails if the bit is zero.
   *
   * @return the parser
   */
  def one(): Parser[Bit] = bit(1)
  
  /**
   * A parser of `count` bits. Fails if the bits in input are less then `count`.
   *
   * @param count the number of bits
   * @return the parser
   */
  def bits(count: Int): Parser[Seq[Bit]] = for {bits <- timesN(bit())(count)} yield bits
  
  /**
   * A single byte parser. Fails if the bits in input are less than 8.
   *
   * @return the parser
   */
  def byte(): Parser[Byte] = for {bits <- bits(8)} yield bits.toBytes.head
  
  /**
   * A single byte parser that fails if the byte is different from `byte`.
   *
   * @param byte the byte wanted
   * @return the parser
   */
  def byte(byte: Byte): Parser[Byte] = conditional(this.byte())(_ == byte)
  
  /**
   * A parser of `count` bytes. Fails if the bytes in input are less than `count`.
   *
   * @param count the number of bytes
   * @return the parser
   */
  def bytes(count: Int): Parser[Seq[Byte]] = for {bytes <- timesN(byte())(count)} yield bytes
  
}
