package mqtt.server

import mqtt.model.ErrorPacket.ChannelClosed
import mqtt.model.Packet
import mqtt.model.Packet.Disconnect
import mqtt.parser.MqttPacketParser
import mqtt.utils.BitImplicits._
import rx.lang.scala.Observable

//TODO: doc
object Receiver {
  
  //TODO: doc
  def apply(idSocket: IdSocket): Observable[(IdSocket, Packet)] = Observable[(IdSocket, Packet)](observer => {
    val in = idSocket.socket.getInputStream
    //TODO: refactor this abomination
    var go = true
    var crashed = true
    while (go) {
      try {
        val buffer = new Array[Byte](2)
        val read = in.read(buffer)
        if (read < 2) {
          throw new Exception("Socket is closed")
        } else {
          val buffer1 = new Array[Byte](buffer(1)) //TODO: fix with variable length integer (parser too)
          in.read(buffer1)
          val bits = (buffer.toSeq ++ buffer1.toSeq).toBitsSeq
          val receivedPacket = MqttPacketParser.parse(bits)
          println(Thread.currentThread() + "    Received: " + receivedPacket + " from " + idSocket.id)
          observer.onNext((idSocket, receivedPacket))
          receivedPacket match {
            case _: Disconnect => crashed = false
            case _ =>
          }
        }
      } catch {
        case ex: Throwable => {
          println(Thread.currentThread() + "    Error: " + ex.getMessage + " from " + idSocket.id)
          idSocket.socket.close()
          if (crashed) {
            observer.onNext((idSocket, ChannelClosed()))
          }
          observer.onCompleted()
          go = false
        }
      }
    }
  })
}
