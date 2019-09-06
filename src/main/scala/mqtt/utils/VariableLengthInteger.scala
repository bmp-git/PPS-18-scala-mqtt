package mqtt.utils

//2.2.3, copied step by step, TODO functional refactor
object VariableLengthInteger {
  def encode(value: Int): Seq[Byte] = {
    var list = Seq[Byte]()
    var X = value
    do {
      var encodedByte: Byte = (X % 128).toByte
      X = X / 128
      if (X > 0) {
        encodedByte = (encodedByte | 128).toByte
      }
      list = list :+ encodedByte
    } while (X > 0)
    
    list
  }
  
  def decode(value: Seq[Byte]): Int = {
    ???
  }
}
