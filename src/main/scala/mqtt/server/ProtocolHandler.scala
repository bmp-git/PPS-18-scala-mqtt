package mqtt.server

import mqtt.broker.state.{Channel, MQTTChannel, State}
import mqtt.broker.BrokerManager
import mqtt.model.Packet
import rx.lang.scala.Observable

//TODO: doc
object ProtocolHandler {
  
  //TODO: doc, ???getState: => State, setState: State => Unit???
  def apply(p: Option[(IdSocket, Packet)], getState: => State, setState: State => Unit,
             socketIdMap: scala.collection.mutable.Map[Int, IdSocket]): Observable[(IdSocket, Packet)] =
    Observable[(IdSocket, Packet)](s => {

      def sendAllPendingTransmissions(pendingTransmissions: Map[Channel, Seq[Packet]], closing: Boolean): Unit = {
        pendingTransmissions.foreach { case (channel, packets) =>
          packets.foreach(packet => {
            s.onNext((socketIdMap(channel.id), packet))
          })
          if (closing) {
            s.onNext((socketIdMap(channel.id), ClosePacket))
          }
        }
      }
      
      def emitPackets(state: State): State = {
        state.takeAllPendingTransmission match {
          case (newState, pendingTransmissions) =>
            sendAllPendingTransmissions(pendingTransmissions, closing = false)
            newState.takeClosing match {
              case (newState, pendingTransmissions) =>
                sendAllPendingTransmissions(pendingTransmissions, closing = true)
                newState
            }
        }
      }
      
      p match {
        case Some((idSocket, packet)) => {
          println(Thread.currentThread() + "    Handling: " + packet + " from " + idSocket.id)
          setState(emitPackets(BrokerManager.handle(getState, packet, MQTTChannel(idSocket.id))))
        }
        case None => {
          println(Thread.currentThread() + "    Checking timeouts")
          setState(emitPackets(BrokerManager.tick(getState)))
        }
      }
    })
}
