package mqtt.utils

import mqtt.utils.IterableImplicits._

import scala.annotation.tailrec

//2.2.3
object VariableLengthInteger {
  def encode(value: Int): Seq[Byte] = {
    @tailrec def process(value: Int, data: Seq[Int]): Seq[Int] = value / 128 match {
      case 0 => data :+ value % 128
      case v: Int => process(v, data :+ (value % 128 | 128))
    }
    process(value, Seq()).map(_.toByte)
  }
  
  private def parse(included: Iterable[Byte]):Option[Int] = {
    included.zipWithIndex.grouped(4).toSeq match {
      case a: Seq[Iterable[(Byte, Int)]] if a.length != 1 => Option.empty
      case a: Seq[Iterable[(Byte, Int)]] => a.flatten.map {
        case (b, p) => (b & 0x7F) * Math.pow(128, p).toInt
      }.reduceOption(_ + _)
    }
  }
  
  def decode(stream: Iterable[Byte]): (Option[Int], Iterable[Byte]) = {
    stream.spanUntil(_ < 0) match {
      case (included, excluded) => {
        (parse(included), excluded)
      }
    }
  }
  
  def decode(stream: Iterator[Byte]): Option[Int] = {
    val parts = stream.span(_ < 0)
    val included = parts._1.toSeq
    val excluded = parts._2.next()
    parse(included :+ excluded)
  }
}