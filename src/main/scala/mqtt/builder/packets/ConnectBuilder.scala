package mqtt.builder.packets

import mqtt.builder.BuilderImplicits._
import mqtt.builder.CommonBuilders._
import mqtt.builder.RichBuilder._
import mqtt.builder.{Builder, IdentityBuilder}
import mqtt.model.Packet.Connect
import mqtt.utils.BitImplicits._

/**
 * The builder of Connect packet as referred in chapter 3.1.
 */
case object ConnectBuilder extends IdentityBuilder[Connect] {
  //3.1.2
  private val protocolName = (p: Connect) => p.protocol.name //3.1.2.1
  private val protocolLevel = (p: Connect) => p.protocol.level.bits.drop(24) //3.1.2.2
  private val cleanSession = (p: Connect) => p.cleanSession //3.1.2.4
  private val willFlag = (p: Connect) => p.willMessage.isDefined //3.1.2.5
  private val willQoS = (p: Connect) => p.willMessage.fold(zero :: zero)(qosBuilder of _.qos) //3.1.2.6
  private val willRetain = (p: Connect) => p.willMessage.fold(false)(_.retain) //3.1.2.7
  private val usernameFlag = (p: Connect) => p.credential.isDefined //3.1.2.8
  private val passwordFlag = (p: Connect) => p.credential.fold(false)(_.password.isDefined) //3.1.2.9
  private val flags = usernameFlag :: passwordFlag :: willRetain :: willQoS :: willFlag :: cleanSession :: zero //3.1.2.3
  private val keepAlive = (p: Connect) => p.keepAlive //3.1.2.10
  
  //3.1.3
  private val clientId = (p: Connect) => p.clientId //3.1.3.1
  private val willTopic = (p: Connect) => p.willMessage.map(_.topic) //3.1.3.2
  private val willPayload = (p: Connect) => p.willMessage.map(_.payload).fold(empty)(bytesBuilder of _) //3.1.3.3
  private val username = (p: Connect) => p.credential.map(_.username) //3.1.3.4
  private val password = (p: Connect) => p.credential.flatMap(_.password).fold(empty)(bytesBuilder of _) //3.1.3.5
  
  
  override val builder: Builder[Connect] =
    controlPacketType(1) :: (4 zeros) :: remainingLength :: //3.1.1
      protocolName :: protocolLevel :: flags :: keepAlive :: //3.1.2
      clientId :: willTopic :: willPayload :: username :: password //3.1.3
}
