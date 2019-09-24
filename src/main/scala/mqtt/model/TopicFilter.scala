package mqtt.model

case class TopicFilter(value: String) {
  def matching(topic: Topic): Boolean = topic matching this
}

object TopicFilter {
  def apply(value: String): Option[TopicFilter] = {
    if (valid(value)) Some(new TopicFilter(value)) else None
  }
  
  def valid(value: String): Boolean = {
    //TODO check wildcards are in the right position
    !(value.isEmpty || value.contains("\u0000"))
  }
}