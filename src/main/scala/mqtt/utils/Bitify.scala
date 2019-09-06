package mqtt.utils

import mqtt.utils.BitImplicits._

class Bitify(data: Seq[Byte]) extends Seq[Bit] {
  val BIT_MASK: Seq[Char] = Seq(0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01)
  
  override def apply(index: Int): Bit = (data(index / 8) & BIT_MASK(index % 8)) > 0
  
  override def length: Int = data.length * 8
  
  override def iterator: Iterator[Bit] = for (byte <- data.iterator;
                                              res <- BIT_MASK.map(_ & byte);
                                              bit = Bit(res > 0)) yield bit
}