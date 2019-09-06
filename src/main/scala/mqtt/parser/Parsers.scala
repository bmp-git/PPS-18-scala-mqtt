package mqtt.parser
import Monad._
import mqtt.utils.Bit

/**
 * A container of generic parsers.
 */
object Parsers {
  
  /**
   * A parser of bits that return a result.
   * @param run the parsing function
   * @tparam A the type of the object return by parsing
   */
  case class Parser[+A](run: Seq[Bit] => List[(A, Seq[Bit])])
  
  /**
   * Making Parser a monad.
   */
  implicit object parserMonad extends Monad[Parser] {
    
    override def unit[A](a: => A): Parser[A] = Parser(s => List((a, s)))
    
    override def flatMap[A, B](ma: Parser[A])(f: A => Parser[B]): Parser[B] =
      Parser(s => ma.run(s) flatMap { case (a, rest) => f(a).run(rest)})
  }
  
  def success[A](a: A): Parser[A] = parserMonad.unit(a)
  def failure[A]: Parser[A] = Parser(_ => List())
  
  /**
   * A conditional parser that fails according to the given predicate.
   * @param p the parser to wrap
   * @param predicate the predicate
   * @tparam A the new parser result type
   * @return the new parser
   */
  def conditional[A](p: Parser[A])(predicate: A => Boolean): Parser[A] =
    p.flatMap(a => if (predicate(a)) success(a) else failure)
  
  /**
   * A parser that return a default value without tampering the input bits if the condition is false,
   * executing the parser p otherwise
   * @param default the default result if condition is false
   * @param p the parser to use if condition is true
   * @param condition the condition
   * @tparam A the new parser result type
   * @return the new parser
   */
  def ifConditionFails[A](default: A, p: Parser[A])(condition: Boolean): Parser[A] =
    if (condition) p else Parser(s => List((default, s)))
  
  /**
   * A parser that combine the result of two different parsers.
   * @param p1 the first parser
   * @param p2 the second parser
   * @tparam A the new parser result type
   * @tparam B the first parser result type
   * @tparam C the second parser result type
   * @return the new parser
   */
  def or[A, B <: A, C <: A](p1: Parser[B], p2: Parser[C]): Parser[A] = Parser(s => p1.run(s) ++ p2.run(s))
  
  /**
   * A parser that combine the result of many different parsers with the same result type B.
   * @param p1 the parsers
   * @tparam A the new parser result type
   * @tparam B the parsers result type
   * @return the new parser
   */
  def or[A, B <: A](p1: Parser[B]*): Parser[A] = {
    if (p1.size == 2) or(p1.head, p1(1)) else Parser(s => p1.head.run(s) ++ or(p1.drop(1):_*).run(s))
  }
  
  /**
   * A parse method that parse to completion a sequence of bits using a parser and return the first result
   * @param p the parser to use
   * @param s the input sequence of bits
   * @tparam A the parser return type
   * @return the parsing result if the sequence is completely parsed
   */
  def parse[A](p: Parser[A], s: Seq[Bit]): Option[A] = p.run(s).collectFirst{case (a, Seq()) => a}
}