package mqtt.model

case class TopicFilter(value: String) {
  val regex: String = {
    "^\\Q" +
      value
        .replace("\\E", """\E\\E\Q""") //Needed for security issue, in case \E is inside topic filter.
        .replace("/#", "\\E.*\\Q")
        .replace("#", "\\E[^\\$].*\\Q")
        .replace("/+", "/\\E[^\\/]*\\Q")
        .replace("+", "\\E([^\\$\\/][^\\/]*)?\\Q") + "\\E$"
  }
  
  def matching(topic: Topic): Boolean = topic.value.matches(regex)
}

object TopicFilter {
  def apply(value: String): Option[TopicFilter] = {
    if (valid(value)) Some(new TopicFilter(value)) else None
  }
  
  def valid(value: String): Boolean = {
    val regex = "^\\/?(([^#\\+]*|\\+)\\/)*([^#\\+]*|\\+|#)?$"
    !(value.isEmpty || value.contains("\u0000") || !value.matches(regex))
  }
}