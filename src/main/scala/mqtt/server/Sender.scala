package mqtt.server

import com.typesafe.scalalogging.LazyLogging
import mqtt.builder.MqttPacketBuilder
import mqtt.model.Packet
import mqtt.utils.BitImplicits._
import rx.lang.scala.Observer

/**
 * An observer that emits the incoming packets to the specified socket.
 *
 * @param idSocket the socket
 */
case class Sender(idSocket: IdSocket) extends Observer[Packet] with LazyLogging {
  
  override def onNext(packet: Packet): Unit = {
    implicit def packetToByteArray(p: Packet): Array[Byte] = MqttPacketBuilder.build(p).toBytes.toArray
    try {
      logger.debug(s"Sending $packet to socket ${idSocket.id}") //scalastyle:ignore
      packet match {
        case ClosePacket =>
          idSocket.socket.close()
        case _ =>
          idSocket.socket.getOutputStream.write(packet)
          idSocket.socket.getOutputStream.flush()
      }
    } catch {
      case ex: Exception => logger.warn(s"Error while sending $packet to socket ${idSocket.id}. Error: ${ex.getMessage}")
    }
  }
  
  
  override def onCompleted(): Unit = logger.debug(s"Completed socket ${idSocket.id}")
  
  override def onError(error: Throwable): Unit = logger.error(s"RxError on socket ${idSocket.id}")
}
