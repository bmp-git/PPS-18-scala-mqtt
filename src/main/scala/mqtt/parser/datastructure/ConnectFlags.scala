package mqtt.parser.datastructure

import mqtt.model.QoS

/**
 * Represents the MQTT connect message flags.
 *
 * @param credentials  the user credentials flags
 * @param willFlags    the will options
 * @param cleanSession if it's a clean session request
 */
case class ConnectFlags(
                         credentials: CredentialFlags,
                         willFlags: Option[WillFlags],
                         cleanSession: Boolean
                       )

/**
 * Represents the options regarding the will message in the MQTT connect message.
 *
 * @param retain if the will message should be retained
 * @param qos    the qos of the will message
 */
case class WillFlags(retain: Boolean, qos: QoS)

/**
 * Represents the users credentials flags in the MQTT connect message.
 *
 * @param username if username is present
 * @param password if password is present
 */
case class CredentialFlags(username: Boolean, password: Boolean)