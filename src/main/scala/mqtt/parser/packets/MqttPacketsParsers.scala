package mqtt.parser.packets

import mqtt.model.Packet._
import mqtt.model.{Packet, QoS}
import mqtt.parser.Monad._
import mqtt.parser.Parsers.{Parser, first, many, many1, skip}
import mqtt.parser.datastructure._
import mqtt.parser.fragments.BitParsers.byte
import mqtt.parser.fragments.MqttCommonParsers._
import mqtt.parser.fragments.MqttFixedHeaderParsers._
import mqtt.parser.fragments.MqttPayloadParsers._
import mqtt.parser.fragments.MqttVariableHeaderParsers._

import scala.concurrent.duration._

/**
 * Contains all MQTT 3.1.1 packets parsers.
 */
object MqttPacketsParsers {
  
  /**
   * Parser of all MQTT 3.1.1 packets.
   *
   * @return the MQTT 3.1.1 packets parser
   */
  def mqtt(): Parser[Packet] = first(
    connect(), connack(), disconnect(),
    publish(), puback(), pubrec(), pubrel(), pubcomp(),
    subscribe(), suback(),
    unsubscribe(), unsuback(),
    pingreq(), pingresp()
  )
  
  /**
   * Parser of the MQTT 3.1.1 connect packet.
   *
   * @return the MQTT 3.1.1 connect packet parser
   */
  def connect(): Parser[Packet] = for {
    _ <- packetType(ConnectMask)
    _ <- reserved()
    _ <- variableLength()
    name <- protocolName()
    version <- protocolLevel()
    flags <- connectFlags()
    keepAlive <- keepAlive()
    clientId <- mqttString()
    willMessage <- willPayload(flags willFlags)
    credentials <- credentials(flags credentials)
  } yield Connect(Protocol(name, version), flags cleanSession, keepAlive seconds, clientId, credentials, willMessage)
  
  /**
   * Parser of the MQTT 3.1.1 connack packet.
   *
   * @return the MQTT 3.1.1 connack packet parser
   */
  def connack(): Parser[Packet] = for {
    _ <- packetType(ConnackMask)
    _ <- reserved()
    _ <- variableLength()
    session <- sessionPresent()
    code <- connectReturnCode()
  } yield Connack(session, code)
  
  /**
   * Parser of the MQTT 3.1.1 disconnect packet.
   *
   * @return the MQTT 3.1.1 disconnect packet parser
   */
  def disconnect(): Parser[Packet] = for {
    _ <- packetType(DisconnectMask)
    _ <- reserved()
    _ <- variableLength()
  } yield Disconnect()
  
  /**
   * Parser of the MQTT 3.1.1 publish packet.
   *
   * @return the MQTT 3.1.1 publish packet parser
   */
  def publish(): Parser[Packet] = for {
    _ <- packetType(PublishMask)
    flags <- publishFlags()
    _ <- variableLength()
    topic <- mqttString()
    id <- skip(packetIdentifier())(flags.qos == QoS(0), default = 0)
    payload <- many(byte())
  } yield Publish(flags duplicate, id, ApplicationMessage(flags retain, flags qos, topic, payload))
  
  /**
   * Parser of the MQTT 3.1.1 puback packet.
   *
   * @return the MQTT 3.1.1 puback packet parser
   */
  def puback(): Parser[Packet] = for {
    _ <- packetType(PubackMask)
    _ <- reserved()
    _ <- variableLength()
    id <- packetIdentifier()
  } yield Puback(id)
  
  /**
   * Parser of the MQTT 3.1.1 pubrec packet.
   *
   * @return the MQTT 3.1.1 pubrec packet parser
   */
  def pubrec(): Parser[Packet] = for {
    _ <- packetType(PubrecMask)
    _ <- reserved()
    _ <- variableLength()
    id <- packetIdentifier()
  } yield Pubrec(id)
  
  /**
   * Parser of the MQTT 3.1.1 pubrel packet.
   *
   * @return the MQTT 3.1.1 pubrel packet parser
   */
  def pubrel(): Parser[Packet] = for {
    _ <- packetType(PubrelMask)
    _ <- reserved2()
    _ <- variableLength()
    id <- packetIdentifier()
  } yield Pubrel(id)
  
  /**
   * Parser of the MQTT 3.1.1 pubcomp packet.
   *
   * @return the MQTT 3.1.1 pubcomp packet parser
   */
  def pubcomp(): Parser[Packet] = for {
    _ <- packetType(PubcompMask)
    _ <- reserved()
    _ <- variableLength()
    id <- packetIdentifier()
  } yield Pubcomp(id)
  
  /**
   * Parser of the MQTT 3.1.1 subscribe packet.
   *
   * @return the MQTT 3.1.1 subscribe packet parser
   */
  def subscribe(): Parser[Packet] = for {
    _ <- packetType(SubscribeMask)
    _ <- reserved2()
    _ <- variableLength()
    id <- packetIdentifier()
    subs <- many1(subscription())
  } yield Subscribe(id, subs)
  
  /**
   * Parser of the MQTT 3.1.1 suback packet.
   *
   * @return the MQTT 3.1.1 suback packet parser
   */
  def suback(): Parser[Packet] = for {
    _ <- packetType(SubackMask)
    _ <- reserved()
    _ <- variableLength()
    id <- packetIdentifier()
    subs <- many1(subscriptionGrantedQoS())
  } yield Suback(id, subs)
  
  /**
   * Parser of the MQTT 3.1.1 unsubscribe packet.
   *
   * @return the MQTT 3.1.1 unsubscribe packet parser
   */
  def unsubscribe(): Parser[Packet] = for {
    _ <- packetType(UnsubscribeMask)
    _ <- reserved2()
    _ <- variableLength()
    id <- packetIdentifier()
    unsubs <- many1(unsubscription())
  } yield Unsubscribe(id, unsubs)
  
  /**
   * Parser of the MQTT 3.1.1 unsuback packet.
   *
   * @return the MQTT 3.1.1 unsuback packet parser
   */
  def unsuback(): Parser[Packet] = for {
    _ <- packetType(UnsubackMask)
    _ <- reserved()
    _ <- variableLength()
    id <- packetIdentifier()
  } yield Unsuback(id)
  
  /**
   * Parser of the MQTT 3.1.1 pingreq packet.
   *
   * @return the MQTT 3.1.1 pingreq packet parser
   */
  def pingreq(): Parser[Packet] = for {
    _ <- packetType(PingreqMask)
    _ <- reserved()
    _ <- variableLength()
  } yield Pingreq()
  
  /**
   * Parser of the MQTT 3.1.1 pingresp packet.
   *
   * @return the MQTT 3.1.1 pingresp packet parser
   */
  def pingresp(): Parser[Packet] = for {
    _ <- packetType(PingrespMask)
    _ <- reserved()
    _ <- variableLength()
  } yield Pingresp()
}
