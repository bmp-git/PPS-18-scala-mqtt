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
 * Contains parsers of MQTT 3.1.1 payload elements.
 */
object MqttPayloadParsers {
  /**
   * A MQTT will payload parser. A will payload can appear inside a MQTT connect payload.
   * The parser produce an optional application message containing topic, message, qos and retain according to `willFlags`.
   *
   * @param willFlags option of the will flags (retain, qos) if a will is specified
   * @return the parser
   */
  def willPayload(willFlags: Option[WillFlags]): Parser[Option[ApplicationMessage]] = for {
    willTopic <- skip(mqttString())(willFlags isEmpty, default = "")
    willMessage <- skip(binaryData())(willFlags isEmpty, Seq())
  } yield willFlags.map(f => ApplicationMessage(f.retain, f.qos, willTopic, willMessage))
  
  /**
   * A MQTT user credentials parser. The user credentials can appear inside a MQTT connect payload.
   * The parser produce an optional credential containing username and optionally a password according to `CredentialFlags`.
   *
   * @param flags the credential flags (username, password)
   * @return the parser
   */
  def credentials(flags: CredentialFlags): Parser[Option[Credential]] = for {
    username <- skip(mqttString())(!flags.username, default = "")
    password <- skip(binaryData())(!flags.password, Seq())
  } yield on(flags.username) {
    Credential(username, on(flags.password)(password))
  }
  
  /**
   * A MQTT subscription granted QoS parser. The granted QoS appear inside a MQTT suback payload.
   * The parser produce an optional QoS because the subscription can fail.
   *
   * @return the parser
   */
  def subscriptionGrantedQoS(): Parser[Option[QoS]] = for {
    code <- first(byte(0), byte(1), byte(2), byte(128 toByte))
  } yield on(code != 128.toByte) {
    QoS(code)
  }
  
  /**
   * A MQTT subscription parser. One or more subscription appears inside a MQTT subscribe payload.
   * The parser produce a pair of (topicFilter, QoS).
   *
   * @return the parser
   */
  def subscription(): Parser[(String, QoS)] = for {
    topic <- mqttString()
    _ <- timesN(zero())(6)
    qos <- qos()
  } yield (topic, qos)
  
  /**
   * A MQTT unsubscription parser. One or more subscription appears inside a MQTT unsubscribe payload.
   * The parser produce a topicFilter.
   *
   * @return the parser
   */
  def unsubscription(): Parser[String] = mqttString()
}
