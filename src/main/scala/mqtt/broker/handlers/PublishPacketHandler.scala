package mqtt.broker.handlers

import mqtt.broker.state.StateImplicits._
import mqtt.broker.Common.publishMessage
import mqtt.broker.state.Violation.{InvalidQoSDupPair, InvalidTopicName}
import mqtt.broker.state.{Channel, State, Violation}
import mqtt.model.Packet.Publish
import mqtt.model.{QoS, Topic}


case class PublishPacketHandler(override val packet: Publish, override val channel: Channel) extends PacketHandler[Publish] with AutoViolationHandler {
  
  override def handle: State => State = {
    for {
      _ <- checkValidQoSDupPair
      _ <- checkValidTopic
      _ <- publishMessage(packet.message)
    } yield ()
  }
  
  def checkValidQoSDupPair: State => Either[Violation, State] = state => {
    if (packet.message.qos == QoS(0) && !packet.duplicate) Right(state) else Left(InvalidQoSDupPair())
  }
  
  def checkValidTopic: State => Either[Violation, (Topic, State)] = state => {
    Topic(packet.message.topic).fold[Either[Violation, (Topic, State)]](Left(InvalidTopicName()))(t => Right((t, state)))
  }
  
  //TODO
  def handleRetain(retain: Boolean): State => Either[Violation, (Topic, State)] = state => {
    ???
  }
}
