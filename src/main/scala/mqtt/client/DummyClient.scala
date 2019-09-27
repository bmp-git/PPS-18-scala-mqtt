package mqtt.client

import java.net.{InetAddress, Socket}

import mqtt.builder.MqttPacketBuilder
import mqtt.model.Packet
import mqtt.server.{IdSocket, Receiver}
import mqtt.utils.BitImplicits._
import rx.lang.scala.Observable

import scala.concurrent.duration.Duration

case class DummyClient(port: Int, actions: Seq[(Duration, Packet)]) extends Thread {
  implicit def packetToByteArray(p: Packet): Array[Byte] = MqttPacketBuilder.build(p).toBytes.toArray
  
  val socket = new Socket(InetAddress.getByName("localhost"), port)
  Thread.sleep(1000) //give time to connect
  
  def inputPacketStream(): Observable[Packet] = {
    Receiver(IdSocket(1234, socket)).map(_._2)
  }
  
  override def run(): Unit = {
    try {
      Thread.sleep(1000) //give time to bind to inputPacketStream
      actions.foreach {
        case (duration, packet) =>
          socket.getOutputStream.write(packet)
          socket.getOutputStream.flush()
          Thread.sleep(duration.toMillis)
      }
      socket.close()
    } catch {
      case _: Exception => {}
    }
  }
  
}
