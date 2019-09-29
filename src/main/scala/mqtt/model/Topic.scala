package mqtt.model

/**
 * Represents a topic.
 *
 * @param value the topic path as a string.
 */
case class Topic(value: String) {
  /**
   * Checks whether this topic matches a specified topic filter.
   *
   * @param filter the topic filter.
   * @return true if matches.
   */
  def matching(filter: TopicFilter): Boolean = filter.matching(this)
}

object Topic {
  /**
   * Creates the topic only if the string specified is a valid topic (no wildcards).
   *
   * @param value the topic path as a string.
   * @return a non empty option if the topic string was valid.
   */
  def apply(value: String): Option[Topic] = {
    if (valid(value)) Some(new Topic(value)) else None
  }
  
  /**
   * Checks whether a string is a valid topic (no wildcards).
   *
   * @param value the topic path as a string.
   * @return true if it is valid.
   */
  def valid(value: String): Boolean = {
    !(value.isEmpty || value.contains("#") || value.contains("+") || value.contains("\u0000"))
  }
}