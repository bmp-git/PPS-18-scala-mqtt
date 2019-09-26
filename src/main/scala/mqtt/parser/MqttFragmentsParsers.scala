package mqtt.parser

import mqtt.model.Packet.{ApplicationMessage, ConnectReturnCode, Credential}
import mqtt.model.QoS
import mqtt.model.Types.{Password, Payload}
import mqtt.parser.BitParsers._
import mqtt.parser.Monad._
import mqtt.parser.Parsers.{Parser, conditional, ifConditionFails, first, fail, assure, timesN}
import mqtt.utils.BitImplicits._
import mqtt.utils.{Bit, MqttString, VariableLengthInteger}

/**
 * A container of MQTT packet fragments parsers.
 */
object MqttFragmentsParsers {
  
  def disconnectPacketType(): Parser[Seq[Bit]] = packetType(DisconnectMask)
  
  def packetType(mask: PacketMask): Parser[Seq[Bit]] = for {
    a <- bit(mask code 0)
    b <- bit(mask code 1)
    c <- bit(mask code 2)
    d <- bit(mask code 3)
  } yield Seq(a, b, c, d)
  
  def reserved(): Parser[Seq[Bit]] = for {reserved <- timesN(zero())(4)} yield reserved
  
  def variableLength(): Parser[Int] = Parser(s => {
    VariableLengthInteger.decode(s.toBytes) match {
      case (length, bytes) => length.fold[Option[(Int, Seq[Bit])]](Option.empty)(l =>
        if (bytes.size == l) Option((l, bytes.toSeq.toBitsSeq)) else Option.empty)
    }
  })
  
  def protocolName(): Parser[String] = conditional(utf8())(_ == "MQTT")
  
  def protocolLevel(): Parser[Int] = for {byte <- bytes(1)} yield byte.head.toInt
  
  def connectFlags(): Parser[ConnectFlags] = for {
    username <- bit()
    password <- bit(); _ <- fail(!username && password)
    willFlags <- willFlags()
    cleanSession <- bit()
    _ <- zero()
  } yield ConnectFlags(CredentialFlags(username, password), willFlags, cleanSession)
  
  def willFlags(): Parser[Option[WillFlags]] = for {
    willRetain <- bit()
    willQos <- qos()
    willFlag <- bit(); _ <- assure(willFlag || (!willFlag && !willRetain && willQos == QoS(0)))
  } yield if (willFlag) Option(WillFlags(willRetain, willQos)) else Option.empty
  
  def qos(): Parser[QoS] = for {
    most <- bit()
    least <- bit(); _ <- fail(most && least)
  } yield QoS(Seq[Bit](most, least).getValue(0, 2).toInt)
  
  def keepAlive(): Parser[Int] = twoBytesInt()
  
  def willPayload(willFlags: Option[WillFlags]): Parser[Option[ApplicationMessage]] = for {
    willTopic <- ifConditionFails("", utf8())(willFlags.isDefined)
    willMessage <- ifConditionFails(Seq(), message())(willFlags.isDefined)
  } yield willFlags.map(f => ApplicationMessage(f.retain, f.qos, willTopic, willMessage))
  
  def utf8(): Parser[String] =
    Parser(s => Option((MqttString.decode(s.toBytes), s.toBytes.drop(MqttString.size(s.toBytes) + 2).toBitsSeq)))
  
  def message(): Parser[Payload] = binaryData()
  
  def binaryData(): Parser[Seq[Byte]] = for {
    length <- twoBytesInt();
    payload <- ifConditionFails(Seq(), bytes(length))(length > 0)
  } yield payload
  
  def twoBytesInt(): Parser[Int] = for {bytes <- bytes(2)} yield bytes.toBitsSeq getValue(0, 16) toInt
  
  def credentials(flags: CredentialFlags): Parser[Option[Credential]] = for {
    username <- ifConditionFails("", utf8())(flags.username)
    password <- ifConditionFails(Seq(), password())(flags.password)
  } yield if (flags.username) Option(Credential(username, Option(flags.password) collect { case true => password })) else Option.empty
  
  def password(): Parser[Password] = binaryData()
  
  def sessionPresent(): Parser[Boolean] = for {
    _ <- timesN(zero())(7)
    session <- bit()
  } yield session
  
  def connectReturnCode(): Parser[ConnectReturnCode] = for {
    code <- first(byte(0), byte(1), byte(2), byte(3), byte(4), byte(5))
  } yield ConnectReturnCode(code)
  
}
