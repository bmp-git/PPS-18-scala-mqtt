package mqtt.parser

import Monad._
import mqtt.model.Packet
import mqtt.model.Packet.{Connack, Connect, Disconnect, Protocol}
import mqtt.parser.MqttFragmentsParsers._
import mqtt.parser.BitParsers._
import mqtt.parser.Parsers.{Parser, or}
import scala.concurrent.duration._

object MqttPacketsParsers {
  
  def connectParser(): Parser[Packet] = for {
    _ <- packetType(ConnectMask)
    _ <- reserved()
    _ <- bytes(1)
    _ <- protocolName()
    version <- protocolLevel()
    flags <- connectFlags()
    keepAlive <- keepAlive()
    clientId <- utf8() //check length and chars?  [MQTT-3.1.3-5]
    willMessage <- willPayload(flags.willFlags)
    credentials <- credentials(flags.credentials)
  } yield Connect(Protocol("MQTT", version), flags.cleanSession, keepAlive seconds, clientId, credentials, willMessage)
  
  def disconnectParser(): Parser[Packet] = for {
    _ <- disconnectPacketType()
    _ <- reserved()
    _ <- bytes(1)
  } yield Disconnect
  
  def connackParser(): Parser[Packet] = for {
    _ <- packetType(ConnackMask)
    _ <- reserved()
    _ <- bytes(1)
    session <- sessionPresent()
    code <- connectReturnCode()
  } yield Connack(session, code)
  
  def mqttParser(): Parser[Packet] = or(disconnectParser(), connectParser(), connackParser())
}
