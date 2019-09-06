package mqtt.broker

//TODO this is a draft to be refined
/**
 * Represents a communication channel.
 *
 * @tparam T the wrapper type of the data
 * @tparam K the data type of the data to be sent
 */
trait Channel[T[_], K] {
  /**
   * Send a message on the channel.
   *
   * @param m the message to send.
   */
  def send(m: T[K])
}
