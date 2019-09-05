package mqtt.utils

object BitImplicits {
  implicit def bitify(i: Int) = new {
    private val BYTE_MASK = Seq(0xFF000000, 0x00FF0000, 0x0000FF00, 0x000000FF)
    private val SHIFT_VALUE = Seq(24, 16, 8, 0)

    def bits: Bitify = new Bitify(BYTE_MASK.zip(SHIFT_VALUE).map { case (m, s) => (i & m) >> s }.map(_.toByte))
  }

  implicit def bitify(i: Byte) = new {
    def bits: Bitify = new Bitify(Seq(i))
  }

  implicit def bitify(data: Seq[Byte]) = new {
    def toBitsSeq: Bitify = new Bitify(data)
  }

  implicit def bitToBoolean(b: Bit): Boolean = b.value

  implicit def booleanToBit(b: Boolean): Bit = Bit(b)

  implicit def intToBit(i: Int): Bit = {
    assert(i == 0 || i == 1)
    i > 0
  }

  implicit def bitSequence(bits: Seq[Bit]) = new {
    def toBinaryString: String = bits.map(_.toString)
      .grouped(8)
      .map(_.mkString(""))
      .mkString("-")

    def getValue(from: Int, length: Int): BigInt = {
      BigInt((Seq(0.toByte) ++
        bits.slice(from, from + (length / 8 + (if (length % 8 == 0) 0 else 1)) * 8).grouped(8).map(_.reverse).map(_.zip(0 until 8).map { case (b, p) => {
          (if (b) 1 else 0) * math.pow(2, p).toInt
        }
        }.sum.toByte)).toArray)
    }
  }

  implicit def bitSeqToByteSeq(bits: Seq[Bit]) = new {
    def toBytes: Seq[Byte] = {
      bits.grouped(8).map(_.getValue(0, 8).toByte).toSeq
    }
  }
}

case class Bit(value: Boolean) {
  override def toString: String = if (value) "1" else "0"
}

class Bitify(data: Seq[Byte]) extends Seq[Bit] {

  import BitImplicits._

  private val BIT_MASK = Seq(0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01)

  override def apply(index: Int): Bit = (data(index / 8) & BIT_MASK(index % 8)) > 0

  override def length: Int = data.length * 8

  override def iterator: Iterator[Bit] = for (byte <- data.iterator;
                                              bit <- BIT_MASK.map(_ & byte)) yield BitImplicits.booleanToBit(bit > 0)
}