package mqtt.server

import mqtt.builder.MqttPacketBuilder
import mqtt.model.Packet
import rx.lang.scala.Observer
import mqtt.utils.BitImplicits._

//TODO: doc
case class Sender(idSocket: IdSocket) extends Observer[Packet] {
  
  override def onNext(packet: Packet): Unit = {
    implicit def packetToByteArray(p: Packet): Array[Byte] = MqttPacketBuilder.build(p).toBytes.toArray
    try {
      println(Thread.currentThread() + "    Sending: " + packet + " to socket " + idSocket.id)
      packet match {
        case ClosePacket =>
          idSocket.socket.close()
        case _ =>
          idSocket.socket.getOutputStream.write(packet)
          idSocket.socket.getOutputStream.flush()
      }
    }
  }
  
  override def onCompleted(): Unit = println(Thread.currentThread() + "    Completed socket " + idSocket.id)
  
  override def onError(error: Throwable): Unit = println(Thread.currentThread() + "    RxError on socket " + idSocket.id)
}
