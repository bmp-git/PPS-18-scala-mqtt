package mqtt.parser.fragments

import mqtt.model.Packet.ConnectReturnCode
import mqtt.model.QoS
import mqtt.model.Types.PackedID
import mqtt.parser.Monad._
import mqtt.parser.Parsers.{Parser, assure, conditional, fail, first, timesN}
import mqtt.parser.datastructure.{ConnectFlags, CredentialFlags, WillFlags}
import mqtt.parser.fragments.BitParsers._
import mqtt.parser.fragments.MqttCommonParsers._
import mqtt.utils.BitImplicits._
import mqtt.utils.RichOption._

/**
 * Contains parsers of MQTT 3.1.1 variable header elements.
 */
object MqttVariableHeaderParsers {
  /**
   * A MQTT protocol name parser. The protocol name is present inside the connect variable header.
   * The parser produce the protocol name string, fails if is not "MQTT"
   *
   * @return the parser
   */
  def protocolName(): Parser[String] = conditional(mqttString())(_ == "MQTT")
  
  /**
   * A MQTT protocol level parser. The protocol level is present inside the connect variable header.
   * The parser produce an int representing the protocol level.
   *
   * @return the parser
   */
  def protocolLevel(): Parser[Int] = for {byte <- byte()} yield byte toInt
  
  /**
   * A MQTT connect flags parser. The connect flags are present inside the connect variable header.
   * The parser produce the connect flags containing the credentials flags, the will flags and the clean session flag.
   *
   * @return the parser
   */
  def connectFlags(): Parser[ConnectFlags] = for {
    username <- bit()
    password <- bit(); _ <- fail(!username && password)
    willFlags <- willFlags()
    cleanSession <- bit()
    _ <- zero()
  } yield ConnectFlags(CredentialFlags(username, password), willFlags, cleanSession)
  
  /**
   * A MQTT will flags parser. The will flags are present inside the connect flags in the connect variable header.
   * The parser produce an optional will flags containing the will qos and retain.
   *
   * @return the parser
   */
  def willFlags(): Parser[Option[WillFlags]] = for {
    willRetain <- bit()
    willQos <- qos()
    willFlag <- bit(); _ <- assure(willFlag || (!willFlag && !willRetain && willQos == QoS(0)))
  } yield on(willFlag) {
    WillFlags(willRetain, willQos)
  }
  
  /**
   * A MQTT keep alive parser. The keep alive is present inside the connect variable header.
   * The parser produce a value indicating the duration of the keep alive.
   *
   * @return the parser
   */
  def keepAlive(): Parser[Int] = mqttInt()
  
  /**
   * A MQTT session present parser. The session present flag is present inside the connack variable header.
   * The parser produce a boolean indicating if the session is present.
   *
   * @return the parser
   */
  def sessionPresent(): Parser[Boolean] = for {
    _ <- timesN(zero())(7)
    session <- bit()
  } yield session
  
  /**
   * A MQTT connect return code parser. The connect return code is present inside the connack variable header.
   * The parser produce the connect return code.
   *
   * @return the parser
   */
  def connectReturnCode(): Parser[ConnectReturnCode] = for {
    code <- first(byte(0), byte(1), byte(2), byte(3), byte(4), byte(5))
  } yield ConnectReturnCode(code)
  
  /**
   * A MQTT packet identifier parser. The packet identifier is present inside the variable header of many packets.
   * The parser produce the packet identifier.
   *
   * @return the parser
   */
  def packetIdentifier(): Parser[PackedID] = conditional(mqttInt())(_ != 0)
}
