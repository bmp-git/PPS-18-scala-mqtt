package mqtt.broker

import java.util.Calendar

import mqtt.broker.Common.closeChannel
import mqtt.broker.handlers._
import mqtt.broker.state.{Channel, State}
import mqtt.model.ErrorPacket.{ChannelClosed, MalformedPacket}
import mqtt.model.Packet
import mqtt.model.Packet._


object BrokerManager extends ProtocolManager {
  override def handle(state: State, packet: Packet, channel: Channel): State = {
    //if the channel is closing the packet cannot be accepted
    val transition = state.closing.get(channel).fold[State => State]({
      packet match {
        case p: Connect => ConnectPacketHandler(p, channel).handle
        case p: Disconnect => DisconnectPacketHandler(p, channel).handle
        case p: Publish => PublishPacketHandler(p, channel).handle
        case p: Subscribe => SubscribePacketHandler(p, channel).handle
        case p: Unsubscribe => UnsubscribePacketHandler(p, channel).handle
        case _: MalformedPacket => println("Received malformed packet from ".concat(channel.toString)); closeChannel(channel)
        case _: ChannelClosed => closeChannel(channel)
        case _ => println("Packet not supported"); identity[State]
      }
    })(_ => identity[State]) andThen tick
  
    transition(state)
  }
  
  override def tick(state: State): State = {
    val now = Calendar.getInstance().getTime
    val chToClose = for {
      (_, session) <- state.sessions
      ch <- session.channel //active sessions
      if session.keepAlive.toMillis > 0 //if 0 keepAlive is disabled
      elapsed = now.getTime - session.lastContact.getTime
      if elapsed > (session.keepAlive.toMillis * 1.5)
    } yield ch
    
    chToClose.map(closeChannel).foldLeft[State => State](identity)(_ andThen _)(state)
  }
}
