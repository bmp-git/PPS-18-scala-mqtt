package mqtt.builder.packets

import mqtt.builder.BuilderImplicits._
import mqtt.builder.CommonBuilders._
import mqtt.builder.RichBuilder._
import mqtt.builder.{Builder, IdentityBuilder}
import mqtt.model.Packet.Suback
import mqtt.model.QoS

/**
 * Provide the structure of Suback packet as referred in chapter 3.9.
 */
case object SubackBuilder extends IdentityBuilder[Suback] {
  //3.9.3
  private val failure = (p: Option[QoS]) => p.isEmpty
  private val qos = (p: Option[QoS]) => p.fold(zero :: zero)(qosBuilder of _)
  
  override val builder: Builder[Suback] =
    controlPacketType(9) :: (4 zeros) :: remainingLength :: //3.9.1
      packetIdentifier :: //3.9.2
      (failure :: (5 zeros) :: qos).foreach[Suback](_.subscriptions) //3.9.3
}
