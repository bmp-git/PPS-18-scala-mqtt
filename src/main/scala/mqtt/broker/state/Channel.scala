package mqtt.broker.state

/**
 * Represents a communication channel.
 */
trait Channel {
  /**
   * The id to identify the channel.
   *
   * @return the identifier.
   */
  def id: Int
}
