package mqtt.parser

import Monad._
import mqtt.model.Packet.{ApplicationMessage, ConnectReturnCode, Credential}
import mqtt.model.QoS
import mqtt.model.Types.{Password, Payload}
import mqtt.parser.BitParsers._
import mqtt.parser.Parsers.{Parser, conditional, ifConditionFails, or}
import mqtt.utils.{Bit, MqttUFT8}
import mqtt.utils.BitImplicits._

/**
 * A container of MQTT packet fragments parsers.
 */
object MqttFragmentsParsers {
  
  def packetType(mask: PacketMask): Parser[Seq[Bit]] = for {
    a <- bit(mask code 0)
    b <- bit(mask code 1)
    c <- bit(mask code 2)
    d <- bit(mask code 3)
  } yield Seq(a, b, c, d)
  
  def disconnectPacketType(): Parser[Seq[Bit]] = packetType(DisconnectMask)
  
  def reserved(): Parser[Seq[Bit]] = for {_ <- zero(); _ <- zero(); _ <- zero(); _ <- zero()} yield Seq(0, 0, 0, 0)
  
  def utf8(): Parser[String] = Parser(s => List((MqttUFT8.decode(s.toBytes), s.toBytes.drop(MqttUFT8.size(s.toBytes) + 2).toBitsSeq)))
  
  def protocolName(): Parser[String] = conditional(utf8())(_ == "MQTT")
  
  def protocolLevel(): Parser[Int] = for {byte <- bytes(1)} yield byte.head.toInt
  
  def qos(): Parser[QoS] = for {
    most <- bit()
    least <- or(conditional(bit())(_ => !most), conditional(zero())(_ => most))
  } yield QoS(Seq[Bit](most, least).getValue(0, 2).toInt)
  
  def willFlags(): Parser[Option[WillFlags]] = for {
    willRetain <- bit()
    willQos <- qos()
    willFlag <- or(conditional(zero())(_ => !willRetain && willQos == QoS(0)), conditional(one())(_ => true))
  } yield if (willFlag) Option(WillFlags(willRetain, willQos)) else Option.empty
  
  def connectFlags(): Parser[ConnectFlags] = for {
    username <- bit()
    password <- or(conditional(zero())(_ => !username), conditional(bit())(_ => username))
    willFlags <- willFlags()
    cleanSession <- bit()
    _ <- zero()
  } yield ConnectFlags(CredentialFlags(username, password), willFlags, cleanSession)
  
  def twoBytesInt(): Parser[Int] = for {bytes <- bytes(2)} yield bytes.toBitsSeq.getValue(0, 16).toInt
  
  def keepAlive(): Parser[Int] = twoBytesInt()
  
  def binaryData(): Parser[Seq[Byte]] = for {
    length <- twoBytesInt();
    payload <- ifConditionFails(Seq(), bytes(length))(length > 0)
  } yield payload
  
  def message(): Parser[Payload] = binaryData()
  
  def password(): Parser[Password] = binaryData()
  
  def willPayload(willFlags: Option[WillFlags]): Parser[Option[ApplicationMessage]] = for {
    willTopic <- ifConditionFails("", utf8())(willFlags.isDefined)
    willMessage <- ifConditionFails(Seq(), message())(willFlags.isDefined)
  } yield willFlags.map(f => Option(ApplicationMessage(f.retain, f.qos, willTopic, willMessage))).head
  
  def credentials(flags: CredentialFlags): Parser[Option[Credential]] = for {
    username <- ifConditionFails("", utf8())(flags.username)
    password <- ifConditionFails(Seq(), password())(flags.password)
  } yield if (flags.username) Option(Credential(username, Option(flags.password) collect { case true => password })) else Option.empty
  
  def sessionPresent(): Parser[Boolean] = for {
    _ <- zero(); _ <- zero(); _ <- zero(); _ <- zero(); _ <- zero(); _ <- zero(); _ <- zero();
    session <- bit()
  } yield session
  
  def connectReturnCode(): Parser[ConnectReturnCode] = for {
    code <- or(byte(0), byte(1), byte(2), byte(3), byte(4), byte(5))
  } yield ConnectReturnCode(code)

}
