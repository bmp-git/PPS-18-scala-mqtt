package mqtt.parser.fragments

import mqtt.model.Packet.{ApplicationMessage, Credential}
import mqtt.model.QoS
import mqtt.parser.Monad._
import mqtt.parser.Parsers.{Parser, first, skip, timesN}
import mqtt.parser.datastructure.{CredentialFlags, WillFlags}
import mqtt.parser.fragments.BitParsers._
import mqtt.parser.fragments.MqttCommonParsers._
import mqtt.utils.RichOption._

/**
 * A container of MQTT 3.1.1 payload elements.
 */
object MqttPayloadParsers {
  def willPayload(willFlags: Option[WillFlags]): Parser[Option[ApplicationMessage]] = for {
    willTopic <- skip(mqttString())(willFlags isEmpty, default = "")
    willMessage <- skip(binaryData())(willFlags isEmpty, Seq())
  } yield willFlags.map(f => ApplicationMessage(f.retain, f.qos, willTopic, willMessage))
  
  def credentials(flags: CredentialFlags): Parser[Option[Credential]] = for {
    username <- skip(mqttString())(!flags.username, default = "")
    password <- skip(binaryData())(!flags.password, Seq())
  } yield on(flags.username) {
    Credential(username, on(flags.password)(password))
  }
  
  def subscriptionGrantedQoS(): Parser[Option[QoS]] = for {
    code <- first(byte(0), byte(1), byte(2), byte(128 toByte))
  } yield on(code != 128.toByte) {
    QoS(code)
  }
  
  def subscription(): Parser[(String, QoS)] = for {
    topic <- mqttString()
    _ <- timesN(zero())(6)
    qos <- qos()
  } yield (topic, qos)
  
  def unsubscription(): Parser[String] = mqttString()
}
