package mqtt.parser.datastructure

import mqtt.model.QoS

/**
 * A structure that represents the MQTT publish message flags.
 *
 * @param duplicate true if the publish is a duplicate, false otherwise
 * @param qos the qos of the publish request
 * @param retain true if the message published should be retained, false otherwise
 */
case class PublishFlags(duplicate: Boolean, qos: QoS, retain: Boolean)
