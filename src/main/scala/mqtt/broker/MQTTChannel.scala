package mqtt.broker

/**
 * Represents a communication channel related to a client.
 *
 * @param id the id to identify the channel
 */
case class MQTTChannel(override val id: Int) extends Channel
