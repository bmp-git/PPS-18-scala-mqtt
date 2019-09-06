import java.net.{ServerSocket, Socket}
import java.util.concurrent.Executors

import mqtt.PacketParser
import rx.lang.scala.Observable
import rx.lang.scala.schedulers._
import java.net._

import mqtt.broker.ProtocolManager
import mqtt.model.Packet
import mqtt.utils.Bit
import mqtt.utils.BitImplicits._

import scala.concurrent.ExecutionContext


object RxMain extends App {
  
  val server = new ServerSocket(9999)
  
  val connectionsExecutor = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  val connectionsScheduler = ExecutionContextScheduler(connectionsExecutor)
  
  val packetHandlerExecutor = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
  val packetHandlerScheduler = ExecutionContextScheduler(packetHandlerExecutor)
  
  (0 to 2).foreach(_ => {
    new Client().start()
  })
  val pm: ProtocolManager = ???
  Observable[Socket](s => {
    Stream.continually(server.accept()).foreach(s.onNext)
  }).map(socket => Observable[(Socket, Packet)](s => {
    println("New client! " + Thread.currentThread())
    val in = socket.getInputStream
    val parser: PacketParser = new PacketParser {
      override def parse(input: Seq[Bit]): Packet = {
        if (input.length < 32) {
          throw new Exception("Malformed packet")
        }
        DummyPacket(input.getValue(24, 8).toByte)
      }
    }
    var go = true
    while (go) {
      try {
        val buffer = new Array[Byte](3)
        in.read(buffer)
        val buffer1 = new Array[Byte](buffer(1) * 256 + buffer(2))
        in.read(buffer1)
        
        s.onNext((socket, parser.parse((buffer.toSeq ++ buffer1.toSeq).toBitsSeq)))
        println((buffer.toSeq ++ buffer1.toSeq))
      } catch {
        case ex: Throwable => {
          println(ex.getMessage)
          s.onCompleted()
          //s.onError(ex)
          socket.close()
          go = false
        }
      }
    }
  }).subscribeOn(connectionsScheduler)).flatMap(p => p).observeOn(packetHandlerScheduler).foreach(p => {
    println(p + Thread.currentThread().toString)
  })
  
}

case class DummyPacket(value: Byte) extends Packet {
  override def toString: String = "Dummy: " + value
}

class Client extends Thread {
  override def run(): Unit = {
    val s = new Socket(InetAddress.getByName("localhost"), 9999)
    Thread.sleep(1000)
    s.getOutputStream.write(Array[Byte](0, 0, 1, 10))
    Thread.sleep(1000)
    s.close()
  }
}