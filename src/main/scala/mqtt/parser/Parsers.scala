package mqtt.parser
import Monad._
import mqtt.utils.Bit


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
  
  def ifConditionFails[A](default: A, p: Parser[A])(predicate: Boolean): Parser[A] = if (predicate) p else Parser(s => List((default, s)))
  
  def or[A, B <: A, C <: A](p1: Parser[B], p2: Parser[C]): Parser[A] = Parser(s => p1.run(s) ++ p2.run(s))
  
  //to test
  def or[A, B <: A](p1: Parser[B]*): Parser[A] = {
    if (p1.size == 2) or(p1.head, p1(1)) else Parser(s => p1.head.run(s) ++ or(p1.tail:_*).run(s))
  }
  
  def parse[A](p: Parser[A], s: Seq[Bit]): Option[A] = p.run(s).collectFirst{case (a, Seq()) => a}
}