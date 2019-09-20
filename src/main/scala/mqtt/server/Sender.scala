package mqtt.server

import mqtt.builder.MqttPacketBuilder
import mqtt.model.Packet
import rx.lang.scala.Observer
import mqtt.utils.BitImplicits._

//TODO: doc
case class Sender(socketId: Int, socketIdMap:scala.collection.mutable.Map[Int, IdSocket]) extends Observer[Packet] {
  private val idSocket = socketIdMap(socketId)
  
  override def onNext(packet: Packet): Unit = {
    implicit def packetToByteArray(p: Packet): Array[Byte] = MqttPacketBuilder.build(p).toBytes.toArray
    try {
      println(Thread.currentThread() + "    Sending: " + packet + " to " + socketId)
      packet match {
        case ClosePacket =>
          idSocket.socket.close()
          socketIdMap -= socketId
        case _ =>
          idSocket.socket.getOutputStream.write(packet)
          idSocket.socket.getOutputStream.flush()
      }
    }
  }
  
  override def onCompleted(): Unit = println(Thread.currentThread() + "    Completed socket " + socketId)
}
