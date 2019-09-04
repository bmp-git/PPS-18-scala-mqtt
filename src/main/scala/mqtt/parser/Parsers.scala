package mqtt.parser
import Monad._
import mqtt.model.Packet
import mqtt.model.Packet.Disconnect
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
  
  // a parser of any single bit , failing on empty lists [Nil or List()?]
  def item(): Parser[Bit] = Parser(s => if (s.isEmpty) List() else List((s.head, s.tail)))
  
  // gets symbol bit
  def bit(bit: Bit): Parser[Bit] = conditional(item())(_ == bit)
  
  def zero(): Parser[Bit] = bit(0)
  def one(): Parser[Bit] = bit(1)
  
  //[Nil or List()?]
  def bits(count: Int): Parser[Seq[Bit]] = Parser(bits => if(bits.size < count) List() else List((bits.take(count), bits.drop(count))))
  
  def parse[A](p: Parser[A], s: Seq[Bit]): Option[A] = p.run(s).collectFirst{case (a, Seq()) => a}
  
  object MqttParser {
    private def packetType(mask: PacketMask): Parser[Seq[Bit]] = for {
      a <- bit(mask code 0)
      b <- bit(mask code 1)
      c <- bit(mask code 2)
      d <- bit(mask code 3)
    } yield Seq(a, b, c, d)
  
    def disconnectPacketType(): Parser[Seq[Bit]] = packetType(DisconnectMask)
  
    def connectPacketType(): Parser[Seq[Bit]] = packetType(ConnectMask)
  
    def reserved(): Parser[Seq[Bit]] = for { _ <- zero(); _ <- zero(); _ <- zero(); _ <- zero() } yield Seq(0, 0, 0, 0)
  
    def disconnectParser(): Parser[Packet] = for {
      _ <- disconnectPacketType()
      _ <- reserved()
      _ <- bits(8)
    } yield Disconnect
  }
}