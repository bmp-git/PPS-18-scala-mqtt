package mqtt.server.client

import java.net.{InetAddress, Socket}
import java.util.concurrent.Executors

import mqtt.model.ErrorPacket.ChannelClosed
import mqtt.model.Packet
import mqtt.server.{IdSocket, Receiver}

import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}
import scala.util.Try

/**
 * An abstraction for a mqtt client.
 * Used only for test purpose
 *
 * @param port the connection's port
 */
abstract class BaseClient(port: Int) extends Thread {
  private val DEFAULT_IO_WAIT_TIME = 1 second
  private val receiver = Executors.newSingleThreadExecutor()
  private val incomingPackets = Promise[Seq[Packet]]()
  
  /**
   * When the connection closes this return all the received packets.
   *
   * @return the received packets
   */
  def receivedPackets(): Seq[Packet] = Await.result(incomingPackets.future, 1 minutes)
  
  override final def run(): Unit = {
    Try {
      val socket = openConnection()
      execute(socket)
      waitIO() //give time to flush
      socket.close()
    }
  }
  
  protected def execute(socket: Socket)
  
  protected def waitIO(): Unit = Thread.sleep(DEFAULT_IO_WAIT_TIME.toMillis)
  
  private def openConnection(): Socket = {
    val socket = new Socket(InetAddress.getByName("localhost"), port)
    waitIO() //give time to connect
    receiver.submit(() => {
      val received = Receiver(IdSocket(0, socket)).map(_._2).toList.toBlocking.single.filter {
        case _: ChannelClosed => false
        case _ => true
      }
      receiver.shutdown()
      incomingPackets.success(received)
    })
    waitIO() //give time to bind to inputStream
    socket
  }
}
