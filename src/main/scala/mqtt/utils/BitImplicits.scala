package mqtt.utils


object BitImplicits {
  
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
      def bitsToByte(bits: Seq[Bit]): Byte =
        bits.zipWithIndex.map {
          case (bit, place) => (if (bit) 1 else 0) * (1 << (7 - place))
        }.sum.toByte
      
      //example: 10100000010
      BigInt((Seq(0.toByte) ++
        value.slice(from, from + length) //from: 1, length: 9 => 010000011 == 131
          .reverse //110000010
          .grouped(8) //11000001-0
          .map(_.padTo(8, Bit(false)).reverse) //11000001-00000000 => 10000011-00000000
          .toSeq.reverse //00000000-10000011
          .map(bitsToByte)).toArray) //0x00 0x83 == 131
    }
    
    def toBytes: Seq[Byte] = {
      value.grouped(8).map(_.getValue(0, 8).toByte).toSeq
    }
  }
  
  implicit def bitToBoolean(b: Bit): Boolean = b.value
  
  implicit def booleanToBit(b: Boolean): Bit = Bit(b)
  
  implicit def intToBit(i: Int): Bit = i > 0
}
