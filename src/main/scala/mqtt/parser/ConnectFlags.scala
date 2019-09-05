package mqtt.parser

import mqtt.model.QoS

case class ConnectFlags(
                       credentials: CredentialFlags,
                       willFlags: Option[WillFlags],
                       cleanSession: Boolean
                       )

case class WillFlags(retain: Boolean, qos: QoS)
case class CredentialFlags(username: Boolean, password: Boolean)