package mqtt.parser

import mqtt.model.QoS

case class ConnectFlags(
                       username: Boolean,
                       password: Boolean,
                       willFlags: Option[WillFlags],
                       cleanSession: Boolean
                       )

case class WillFlags(retain: Boolean, qos: QoS)