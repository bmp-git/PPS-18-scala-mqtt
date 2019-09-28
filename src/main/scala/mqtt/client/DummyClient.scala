package mqtt.client

import java.net.{InetAddress, Socket}
import java.util.concurrent.Executors

import mqtt.builder.MqttPacketBuilder
import mqtt.model.ErrorPacket.ChannelClosed
import mqtt.model.Packet
import mqtt.model.Packet.Disconnect
import mqtt.server.{IdSocket, Receiver}
import mqtt.utils.BitImplicits._

import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}

case class DummyClient(port: Int, actions: Seq[Packet]) extends Thread {
  implicit def packetToByteArray(p: Packet): Array[Byte] = MqttPacketBuilder.build(p).toBytes.toArray
  
  
  private val receiver = Executors.newSingleThreadExecutor()
  private val incomingPackets = Promise[Seq[Packet]]()
  
  def receivedPackets(): Seq[Packet] = {
    val received = Await.result(incomingPackets.future, 1 minutes)
    receiver.shutdown()
    received
  }
  
  override def run(): Unit = {
    val socket = new Socket(InetAddress.getByName("localhost"), port)
    Thread.sleep(500) //give time to connect
    receiver.submit(() => {
      val received = Receiver(IdSocket(0, socket)).map(_._2).toList.toBlocking.single.filter {
        case _: ChannelClosed => false
        case _ => true
      }
      incomingPackets.success(received)
    })
    Thread.sleep(100) //give time to bind to inputPacketStream
    
    try {
      actions.foreach { packet =>
        packet match {
          case Disconnect() => Thread.sleep(500) //give time to flush remaining bytes
          case _ =>
        }
        socket.getOutputStream.write(packet)
        socket.getOutputStream.flush()
      }
      socket.close()
    } catch {
      case _: Exception =>
    }
  }
  
}
