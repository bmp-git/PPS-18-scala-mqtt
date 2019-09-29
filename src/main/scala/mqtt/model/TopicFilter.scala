package mqtt.model

/**
 * Represents a topic filter, that could match multiple topics.
 *
 * @param value the topic filter as a string.
 */
case class TopicFilter(value: String) {
  /**
   * The regex used to check whether this filter matches a topic.
   */
  val regex: String = {
    "^\\Q" +
      value
        .replace("\\E", """\E\\E\Q""") //Needed for security issue, in case \E is inside topic filter.
        .replace("/#", "\\E.*\\Q")
        .replace("#", "\\E[^\\$].*\\Q")
        .replace("/+", "/\\E[^\\/]*\\Q")
        .replace("+", "\\E([^\\$\\/][^\\/]*)?\\Q") + "\\E$"
  }
  
  /**
   * Checks if this filter matches a specified topic.
   *
   * @param topic the topic.
   * @return true if it matches.
   */
  def matching(topic: Topic): Boolean = topic.value.matches(regex)
}

object TopicFilter {
  /**
   * Creates the topic filter only if the string specified is a valid filter.
   *
   * @param value the topic filter as a string.
   * @return a non empty option if the topic filter string was valid.
   */
  def apply(value: String): Option[TopicFilter] = {
    if (valid(value)) Some(new TopicFilter(value)) else None
  }
  
  /**
   * Checks whether a string is a valid topic filter.
   *
   * @param value the topic filter as a string.
   * @return true if it is valid.
   */
  def valid(value: String): Boolean = {
    val regex = "^\\/?(([^#\\+]*|\\+)\\/)*([^#\\+]*|\\+|#)?$"
    !(value.isEmpty || value.contains("\u0000") || !value.matches(regex))
  }
}