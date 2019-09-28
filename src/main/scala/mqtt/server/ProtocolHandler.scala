package mqtt.server

import mqtt.broker.{BrokerManager, Channel, MQTTChannel, State}
import mqtt.model.Packet
import rx.lang.scala.Observable

/**
 * Handles the protocol logic with reactivex.
 */
object ProtocolHandler {
  
  /**
   * Produces a stream of reactions from an input packet and a given state.
   *
   * @param p       the pair (socket, packet) representing the received packet from the socket
   * @param program the state of the program
   * @return a new observer representing the packets to send to the client. (socket id, packet to send)
   */
  def apply(p: Option[(IdSocket, Packet)], program: ProgramState): Observable[(Int, Packet)] =
    Observable[(Int, Packet)](s => {

      def sendAllPendingTransmissions(pendingTransmissions: Map[Channel, Seq[Packet]], closing: Boolean): Unit = {
        pendingTransmissions.foreach { case (channel, packets) =>
          packets.foreach(packet => {
            s.onNext((channel.id, packet))
          })
          if (closing) {
            s.onNext((channel.id, ClosePacket))
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
          program.brokerState = emitPackets(BrokerManager.handle(program.brokerState, packet, MQTTChannel(idSocket.id)))
        }
        case None => {
          println(Thread.currentThread() + "    Checking timeouts")
          program.brokerState = emitPackets(BrokerManager.tick(program.brokerState))
        }
      }
    })
}
