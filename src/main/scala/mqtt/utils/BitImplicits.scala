package mqtt.utils

//TODO: rename to bitImplicits
package object BitImplicits {
  
  implicit class RichBitwiseInt(value: Int) {
    private val BYTE_MASK = Seq(0xFF000000, 0x00FF0000, 0x0000FF00, 0x000000FF)
    private val SHIFT_VALUE = Seq(24, 16, 8, 0)
    
    def bits: Bitify = new Bitify(BYTE_MASK.zip(SHIFT_VALUE).map { case (m, s) => (value & m) >> s }.map(_.toByte))
  }
  
  implicit class RichBitwiseByte(value: Byte) {
    def bits: Bitify = new Bitify(Seq(value))
  }
  
  implicit class RichBitwiseSeqBytes(value: Seq[Byte]) {
    def toBitsSeq: Bitify = new Bitify(value)
  }
  
  implicit class RichBitwiseSeqBits(value: Seq[Bit]) {
    def toBinaryString: String = value.map(_.toString).reverse
      .grouped(8).map(_.reverse).toSeq.reverse
      .map(_.mkString(""))
      .mkString("-")
    
    def getValue(from: Int, length: Int): BigInt = {
      BigInt((Seq(0.toByte) ++
        value.slice(from, from + length).reverse
          .grouped(8).map(_.padTo(8, Bit(false)).reverse).toSeq.reverse
          .map(_.zipWithIndex.map {
            case (b, p) => {
              (if (b) 1 else 0) * math.pow(2, 7 - p).toInt
            }
          }.sum.toByte)).toArray)
    }
    
    def toBytes: Seq[Byte] = {
      value.grouped(8).map(_.getValue(0, 8).toByte).toSeq
    }
  }
  
  implicit def bitToBoolean(b: Bit): Boolean = b.value
  
  implicit def booleanToBit(b: Boolean): Bit = Bit(b)
  
  implicit def intToBit(i: Int): Bit = i > 0
}
