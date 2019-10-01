package mqtt.model

/**
 * Broker configuration.
 *
 * @param port           listen port
 * @param bindAddress    listen address
 * @param allowAnonymous allow anonymous user
 */
case class BrokerConfig(
                         port: Int = 1883,
                         bindAddress: Option[String] = Option.empty,
                         allowAnonymous: Boolean = true
                       )