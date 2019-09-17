package mqtt.parser

import mqtt.model.Packet
import mqtt.model.Packet.{Connack, Connect, Disconnect, Protocol}
import mqtt.parser.BitParsers._
import mqtt.parser.Monad._
import mqtt.parser.MqttFragmentsParsers._
import mqtt.parser.Parsers.{Parser, or}

import scala.concurrent.duration._

/**
 * A container of MQTT 3.1.1 packets parsers.
 */
object MqttPacketsParsers {
  
  /**
   * Parser of all MQTT 3.1.1 packets.
   *
   * @return the MQTT 3.1.1 packets parser
   */
  def mqttParser(): Parser[Packet] = or(disconnectParser(), connectParser(), connackParser())
  
  /**
   * Parser of the MQTT 3.1.1 connect packet.
   *
   * @return the MQTT 3.1.1 connect packet
   */
  def connectParser(): Parser[Packet] = for {
    _ <- packetType(ConnectMask)
    _ <- reserved()
    _ <- variableLength()
    _ <- protocolName()
    version <- protocolLevel()
    flags <- connectFlags()
    keepAlive <- keepAlive()
    clientId <- utf8() //check length and chars?  [MQTT-3.1.3-5]
    willMessage <- willPayload(flags.willFlags)
    credentials <- credentials(flags.credentials)
  } yield Connect(Protocol("MQTT", version), flags.cleanSession, keepAlive seconds, clientId, credentials, willMessage)
  
  /**
   * Parser of the MQTT 3.1.1 disconnect packet.
   *
   * @return the MQTT 3.1.1 disconnect packet
   */
  def disconnectParser(): Parser[Packet] = for {
    _ <- disconnectPacketType()
    _ <- reserved()
    _ <- variableLength()
  } yield Disconnect()
  
  /**
   * Parser of the MQTT 3.1.1 connack packet.
   *
   * @return the MQTT 3.1.1 connack packet
   */
  def connackParser(): Parser[Packet] = for {
    _ <- packetType(ConnackMask)
    _ <- reserved()
    _ <- variableLength()
    session <- sessionPresent()
    code <- connectReturnCode()
  } yield Connack(session, code)
}
