package mqtt.parser

import mqtt.parser.Monad._
import mqtt.utils.Bit

/**
 * Contains generic parsers.
 */
object Parsers {
  
  /**
   * Represents a deterministic parser.
   * A deterministic parser of bits that optionally produce a result.
   * A deterministic parser has a function run that produce a result and (may) consume the input string or produce nothing.
   *
   * @param run the parsing function
   * @tparam A the type of the object return by parsing
   */
  case class Parser[+A](run: Seq[Bit] => Option[(A, Seq[Bit])])
  
  /**
   * Implicitly making Parser a monad.
   */
  implicit object parserMonad extends Monad[Parser] {
    
    /**
     * The unit parser is simply a parser that produce `a` and do not consume the input.
     *
     * @param a the parser result
     * @tparam A the parser result type
     * @return the unit parser
     */
    override def unit[A](a: => A): Parser[A] = Parser(s => Option((a, s)))
    
    /**
     * The parser representing the link between the result obtained running the first parser and the execution of the next parser.
     *
     * @param ma the current parser
     * @param f  the function from the result of the current parser and the next parser
     * @tparam A the current parser result type
     * @tparam B the next parser result type
     * @return the next parser
     */
    override def flatMap[A, B](ma: Parser[A])(f: A => Parser[B]): Parser[B] =
      Parser(s => ma.run(s) flatMap { case (a, rest) => f(a).run(rest) })
  }
  
  /**
   * A success parser.
   *
   * @param a the result the parser will produce
   * @tparam A the parser result type
   * @return the parser
   */
  def success[A](a: A): Parser[A] = parserMonad.unit(a)
  
  /**
   * A failure parser.
   *
   * @tparam A the parser result type
   * @return the parser
   */
  def failure[A]: Parser[A] = Parser(_ => Option.empty)
  
  /**
   * A conditional parser that fails according to the given predicate.
   *
   * @param p         the parser to wrap
   * @param predicate the predicate
   * @tparam A the parser result type
   * @return the new parser
   */
  def conditional[A](p: Parser[A])(predicate: A => Boolean): Parser[A] =
    p.flatMap(a => if (predicate(a)) success(a) else failure)
  
  /**
   * A parser that skip the parser and return a default value without tampering the input bits if the condition is true,
   * executing the parser p otherwise
   *
   * @param p         the parser
   * @param condition the condition to evaluate
   * @param default   the default value
   * @tparam A the parser result type
   * @return the new parser
   */
  def skip[A](p: Parser[A])(condition: Boolean, default: A): Parser[A] =
    if (!condition) p else success[A](default)
  
  /**
   * A parser that assure if a condition hold.
   *
   * @param condition the condition that must be evaluated
   * @return the new parser that fails and breaks the parsing chain if the condition is false
   */
  def assure(condition: Boolean): Parser[Unit] = if (condition) success[Unit](()) else failure
  
  /**
   * A parser that fails if a condition hold.
   *
   * @param condition the condition that must be evaluated
   * @return the new parser that fails and breaks the parsing chain if the condition is true
   */
  def fail(condition: Boolean): Parser[Unit] = assure(!condition)
  
  
  /**
   * A parser that represent the first successful parser execution of two parsers.
   *
   * @param p1 the first parser
   * @param p2 the second parser
   * @tparam A the new parser result type
   * @tparam B the first parser result type
   * @tparam C the second parser result type
   * @return the new parser
   */
  def first[A, B <: A, C <: A](p1: Parser[B], p2: Parser[C]): Parser[A] = Parser(s => p1.run(s) orElse p2.run(s))
  
  /**
   * A parser that represent the first successful parser execution
   * of many different parsers with the same result type B
   *
   * @param ps the parsers
   * @tparam A the new parser result type
   * @tparam B the parsers result type
   * @return the new parser
   */
  def first[A, B <: A](ps: Parser[B]*): Parser[A] = {
    if (ps.size == 2) first(ps.head, ps(1)) else Parser(s => ps.head.run(s) orElse first(ps.drop(1): _*).run(s))
  }
  
  /**
   * A parser that represent the sequential execution of a list of parsers.
   *
   * @param ps the parsers
   * @tparam A the parsers result type
   * @return the new parser
   */
  def seqN[A](ps: Parser[A]*): Parser[List[A]] = sequence(ps.toList)
  
  /**
   * A parser that represent the sequential execution of a parser n times.
   *
   * @param p the parser
   * @param n the number of execution
   * @tparam A the parser result type
   * @return the new parser
   */
  def timesN[A](p: Parser[A])(n: Int): Parser[List[A]] = sequence(List.fill(n)(p))
  
  /**
   * An optional parser that parse like p or like a parser that return a default value without consuming input.
   *
   * @param default the default value
   * @param p       the parser
   * @tparam A the parser result and default type
   * @return the new parser
   */
  def optional[A](default: A, p: Parser[A]): Parser[A] = first(p, success[A](default))
  
  /**
   * A parser that represent the sequential execution of a parser 0, 1 or many times.
   *
   * @param p the parser
   * @tparam A the parser result type
   * @return the new parser
   */
  def many[A](p: Parser[A]): Parser[List[A]] = optional(List[A](), many1(p))
  
  /**
   * A parser that represent the sequential execution of a parser 1 or many times.
   *
   * @param p the parser
   * @tparam A the result type
   * @return the new parser
   */
  def many1[A](p: Parser[A]): Parser[List[A]] = map2(p)(many(p))(_ :: _)
  
  /**
   * A parse method that parse to completion a sequence of bits using a parser and return the result.
   *
   * @param p the parser to use
   * @param s the input sequence of bits
   * @tparam A the parser return type
   * @return the parsing result if the sequence is completely parsed
   */
  def parse[A](p: Parser[A], s: Seq[Bit]): Option[A] = p run s collect { case (a, Seq()) => a }
}