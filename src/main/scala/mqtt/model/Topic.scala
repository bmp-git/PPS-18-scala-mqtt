package mqtt.model

case class Topic(value: String) {
  def matching(filter: TopicFilter): Boolean = ??? //TODO
}

object Topic {
  def apply(value: String): Option[Topic] = {
    if (valid(value)) Some(new Topic(value)) else None
  }
  
  def valid(value: String): Boolean = {
    !(value.isEmpty || value.contains("#") || value.contains("?") || value.contains("\u0000"))
  }
}