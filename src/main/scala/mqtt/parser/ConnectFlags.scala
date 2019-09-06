package mqtt.parser

import mqtt.model.QoS

/**
 * A structure that represents the MQTT connect message flags.
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
 * Options regarding the will message.
 *
 * @param retain if the will message should be retained
 * @param qos    the qos of the will message
 */
case class WillFlags(retain: Boolean, qos: QoS)

/**
 * Users credentials flags.
 *
 * @param username the flag representing if username is present
 * @param password the flag representing if password is present
 */
case class CredentialFlags(username: Boolean, password: Boolean)