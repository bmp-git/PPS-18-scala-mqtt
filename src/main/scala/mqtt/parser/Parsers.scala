package mqtt.parser
import Monad._
import mqtt.model.Packet
import mqtt.model.Packet.Disconnect
import mqtt.parser.Parsers.MqttParser.{disconnectPacketType, reserved}
import mqtt.utils.Bit
import mqtt.utils.BitImplicits._

object Parsers {
  
  case class Parser[+A](run: Seq[Bit] => List[(A, Seq[Bit])])
  
  implicit object parserMonad extends Monad[Parser] {
    
    override def unit[A](a: => A): Parser[A] = Parser(s => List((a, s)))
    
    override def flatMap[A, B](ma: Parser[A])(f: (A) => Parser[B]): Parser[B] =
      Parser(s => ma.run(s) flatMap { case (a, rest) => f(a).run(rest)})
  }
  
  // gets an element if it satisfies a given predicate
  def conditional[A](p: Parser[A])(predicate: A => Boolean): Parser[A] =
    p.flatMap(a => if (predicate(a)) success(a) else failure)
  
  def success[A](a: A): Parser[A] = parserMonad.unit(a)
  
  def failure[A]: Parser[A] = Parser(s => List())
  
  // a parser of any single bit , failing on empty lists
  def item(): Parser[Bit] = Parser(s => if (s.isEmpty) Nil else List((s.head, s.tail)))
  
  // gets symbol bit
  def bit(bit: Bit): Parser[Bit] = conditional(item())(_ == bit)
  
  def bits(count: Int): Parser[Seq[Bit]] = Parser(bits => if(bits.size < count) Nil else List((bits.take(count), bits.drop(count))))
  
  def parse[A](p: Parser[A], s: Seq[Bit]): Option[A] = p.run(s).collectFirst{case (a, Seq()) => a}
}
