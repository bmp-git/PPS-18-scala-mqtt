package mqtt.parser

/**
 * Represents a generic parser.
 *
 * @tparam I the parser input type
 * @tparam O the parser output type
 */
trait Parser[I, O] {
  /**
   * Execute the parsing of the sequence in input producing an output.
   *
   * @param input the input sequence
   * @return the parsing result
   */
  def parse(input: Seq[I]): O
}
