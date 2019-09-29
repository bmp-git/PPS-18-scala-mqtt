package mqtt.client

import java.net.{InetAddress, Socket}
import java.util.concurrent.Executors

import mqtt.model.ErrorPacket.ChannelClosed
import mqtt.model.Packet
import mqtt.server.{IdSocket, Receiver}

import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}

abstract class BaseClient(port: Int) extends Thread {
  private val receiver = Executors.newSingleThreadExecutor()
  private val incomingPackets = Promise[Seq[Packet]]()
  
  def receivedPackets(): Seq[Packet] = Await.result(incomingPackets.future, 1 minutes)
  
  protected def init(): Socket = {
    val socket = new Socket(InetAddress.getByName("localhost"), port)
    Thread.sleep(500) //give time to connect
    receiver.submit(() => {
      val received = Receiver(IdSocket(0, socket)).map(_._2).toList.toBlocking.single.filter {
        case _: ChannelClosed => false
        case _ => true
      }
      receiver.shutdown()
      incomingPackets.success(received)
    })
    Thread.sleep(100) //give time to bind to inputStream
    socket
  }
  
}
