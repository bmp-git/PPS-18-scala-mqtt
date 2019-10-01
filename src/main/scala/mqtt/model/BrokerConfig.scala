package mqtt.model

case class BrokerConfig(
                         port: Int = 1883,
                         bindAddress: Option[String] = Option.empty,
                         allowAnonymous: Boolean = true
                       )