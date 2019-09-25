package mqtt.model

case class TopicFilter(value: String) {
  def matching(topic: Topic): Boolean = topic matching this
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