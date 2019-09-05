package mqtt.utils

import BitImplicits._


class Bitify(data: Seq[Byte]) extends Seq[Bit] {
  private val BIT_MASK = Seq(0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01)
  
  override def apply(index: Int): Bit = (data(index / 8) & BIT_MASK(index % 8)) > 0
  
  override def length: Int = data.length * 8
  
  override def iterator: Iterator[Bit] = for (byte <- data.iterator;
                                              bit <- BIT_MASK.map(_ & byte)) yield BitImplicits.booleanToBit(bit > 0)
}