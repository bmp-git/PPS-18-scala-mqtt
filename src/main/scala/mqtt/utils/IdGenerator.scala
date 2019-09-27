package mqtt.utils

/**
 * Generate an unique id.
 *
 * @param id the id that will be generated
 */
case class IdGenerator(id: Int) {
  /**
   * Gets the unique id and a new IdGenerator that will generate a different id.
   *
   * @return the unique id and the new generator
   */
  def next(): (Int, IdGenerator) = {
    (id, IdGenerator(id + 1))
  }
}
