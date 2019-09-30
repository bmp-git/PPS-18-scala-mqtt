package mqtt.parser.fragments

import mqtt.model.Packet.ConnectReturnCode
import mqtt.model.QoS
import mqtt.model.Types.PackedID
import mqtt.parser.Monad._
import mqtt.parser.Parsers.{Parser, assure, conditional, fail, first, timesN}
import mqtt.parser.datastructure
import mqtt.parser.datastructure.{ConnectFlags, CredentialFlags, WillFlags}
import mqtt.parser.fragments.BitParsers._
import mqtt.parser.fragments.MqttCommonParsers._
import mqtt.utils.BitImplicits._
import mqtt.utils.RichOption._

/**
 * A container of MQTT 3.1.1 variable header elements.
 */
object MqttVariableHeaderParsers {
  def protocolName(): Parser[String] = conditional(mqttString())(_ == "MQTT")
  
  def protocolLevel(): Parser[Int] = for {byte <- byte()} yield byte toInt
  
  def connectFlags(): Parser[ConnectFlags] = for {
    username <- bit()
    password <- bit(); _ <- fail(!username && password)
    willFlags <- willFlags()
    cleanSession <- bit()
    _ <- zero()
  } yield datastructure.ConnectFlags(CredentialFlags(username, password), willFlags, cleanSession)
  
  def willFlags(): Parser[Option[WillFlags]] = for {
    willRetain <- bit()
    willQos <- qos()
    willFlag <- bit(); _ <- assure(willFlag || (!willFlag && !willRetain && willQos == QoS(0)))
  } yield on(willFlag) {
    WillFlags(willRetain, willQos)
  }
  
  def keepAlive(): Parser[Int] = mqttInt()
  
  def sessionPresent(): Parser[Boolean] = for {
    _ <- timesN(zero())(7)
    session <- bit()
  } yield session
  
  def connectReturnCode(): Parser[ConnectReturnCode] = for {
    code <- first(byte(0), byte(1), byte(2), byte(3), byte(4), byte(5))
  } yield ConnectReturnCode(code)
  
  def packetIdentifier(): Parser[PackedID] = conditional(mqttInt())(_ != 0)
}
