package mqtt.parser
import Monad._
import mqtt.model.ErrorPacket.MalformedPacket
import mqtt.model.{Packet, QoS}
import mqtt.model.Packet.{Disconnect, Puback}
import mqtt.utils.{Bit, MqttUFT8}
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
  
  def or[A, B <: A, C <: A](p1: Parser[B], p2: Parser[C]): Parser[A] = Parser(s => p1.run(s) ++ p2.run(s))
  
  //to test
  def or[A, B <: A](p1: Parser[B]*): Parser[A] = {
    if (p1.size == 2) or(p1.head, p1(1)) else Parser(s => p1.head.run(s) ++ or(p1.tail:_*).run(s))
  }
  
  // a parser of any single bit , failing on empty lists [Nil or List()?]
  def item(): Parser[Bit] = Parser(s => if (s.isEmpty) List() else List((s.head, s.tail)))
  
  // gets symbol bit
  def bit(bit: Bit): Parser[Bit] = conditional(item())(_ == bit)
  
  def zero(): Parser[Bit] = bit(0)
  def one(): Parser[Bit] = bit(1)
  
  //[Nil or List()?]
  def bits(count: Int): Parser[Seq[Bit]] = Parser(bits => if(bits.size < count) List() else List((bits.take(count), bits.drop(count))))
  
  def bytes(count: Int): Parser[Seq[Byte]] = for { bytes <- bits(count * 8) } yield bytes.toBytes
  
  def parse[A](p: Parser[A], s: Seq[Bit]): Option[A] = p.run(s).collectFirst{case (a, Seq()) => a}
  
  object MqttParser {
    private def packetType(mask: PacketMask): Parser[Seq[Bit]] = for {
      a <- bit(mask code 0)
      b <- bit(mask code 1)
      c <- bit(mask code 2)
      d <- bit(mask code 3)
    } yield Seq(a, b, c, d)
  
    def disconnectPacketType(): Parser[Seq[Bit]] = packetType(DisconnectMask)
  
    def reserved(): Parser[Seq[Bit]] = for { _ <- zero(); _ <- zero(); _ <- zero(); _ <- zero() } yield Seq(0, 0, 0, 0)
    
    def utf8(): Parser[String] = Parser(s => List((MqttUFT8.decode(s.toBytes), s.toBytes.drop(MqttUFT8.size(s.toBytes) + 2).toBitsSeq)))
    
    def protocolName(): Parser[String] = conditional(utf8())(_ == "MQTT")
    
    def protocolLevel(): Parser[Int] = for { byte <- bytes(1)} yield byte.head.toInt
    
    def qos(): Parser[QoS] = for {
      most <- item()
      least <- or(conditional(item())(_ => !most), conditional(zero())(_ => most))
    } yield QoS(Seq[Bit](most, least).getValue(0,2).toInt)
  
    def willFlags(): Parser[Option[WillFlags]] = for {
      willRetain <- item()
      willQos <- qos()
      willFlag <- or(conditional(zero())(_ => !willRetain && willQos == QoS(0)), conditional(one())(_ => true))
    } yield if (willFlag) Option(WillFlags(willRetain, willQos)) else Option.empty
    
    def connectFlags(): Parser[ConnectFlags] = for {
      username <- item()
      password <- item()
      willFlags <- willFlags()
      cleanSession <- item()
      _ <- bit(0)
    } yield ConnectFlags(username, password, willFlags, cleanSession)
    
    def connectParser(): Parser[Packet] = for {
      _ <- packetType(ConnectMask)
      _ <- reserved()
      _ <- bytes(1)
      _ <- protocolName()
      version <- protocolLevel()
    } yield MalformedPacket
  
    def disconnectParser(): Parser[Packet] = for {
      _ <- disconnectPacketType()
      _ <- reserved()
      _ <- bytes(1)
    } yield Disconnect
  
    def pubackParser(): Parser[Packet] = for {
      _ <- packetType(PubackMask)
      _ <- reserved()
      _ <- bytes(1)
      packet_id <- bytes(2)
    } yield Puback(0)
    
    def mqttParser(): Parser[Packet] = or(disconnectParser(), pubackParser())
  }
}