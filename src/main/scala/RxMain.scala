import java.net.{ServerSocket, Socket, _}
import java.util.concurrent.Executors

import mqtt.broker._
import mqtt.builder.MqttPacketBuilder
import mqtt.model.Packet
import mqtt.model.Packet.{Connect, Disconnect, Protocol}
import mqtt.parser.MqttPacketParser
import mqtt.utils.BitImplicits._
import rx.lang.scala.schedulers._
import rx.lang.scala.subjects.PublishSubject
import rx.lang.scala.{Observable, Observer, Subject}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object RxMain extends App {
  
  val server = new ServerSocket(9999)
  
  val acceptConnectionsExecutor = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
  val acceptConnectionsScheduler = ExecutionContextScheduler(acceptConnectionsExecutor)
  
  val ioExecutor = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  val ioScheduler = ExecutionContextScheduler(ioExecutor)
  
  val packetHandlerExecutor = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
  val packetHandlerScheduler = ExecutionContextScheduler(packetHandlerExecutor)
  
  (0 until 5).foreach(_ => {
    new Client().start()
  })
  
  object SocketIdGenerator {
    private var count = 0
    
    def next(): Int = {
      count += 1
      count
    }
  }
  case class IdSocket(id: Int, socket: Socket)
  
  case object ClosePacket extends Packet
  
  def clientStream(server: ServerSocket): Observable[IdSocket] = Observable[IdSocket](s => {
    Stream.continually(server.accept()).foreach(socket => {
      val id = SocketIdGenerator.next()
      val idSocket = IdSocket(id, socket)
      socketIdMap += (id -> idSocket)
      println(Thread.currentThread() + "    New client: " + idSocket.id)
      s.onNext(idSocket)
    })
  })
  
  def incomingPacketsObservable(idSocket: IdSocket): Observable[(IdSocket, Packet)] = Observable[(IdSocket, Packet)](observer => {
    val in = idSocket.socket.getInputStream
    //TODO: refactor this abomination
    var go = true
    while (go) {
      try {
        val buffer = new Array[Byte](2)
        val read = in.read(buffer)
        if(read < 2) {
          throw new Exception("Socket is closed")
        } else {
          val buffer1 = new Array[Byte](buffer(1)) //TODO: fix with variable length integer (parser too)
          in.read(buffer1)
          val bits = (buffer.toSeq ++ buffer1.toSeq).toBitsSeq
          val receivedPacket = MqttPacketParser.parse(bits)
          println(Thread.currentThread() + "    Received: " + receivedPacket + " from " + idSocket.id)
          observer.onNext((idSocket, receivedPacket))
        }
      } catch {
        case ex: Throwable => {
          println(Thread.currentThread() + "    Error: " + ex.getMessage + " from " + idSocket.id)
          observer.onCompleted()
          idSocket.socket.close()
          go = false
        }
      }
    }
  })
  var state: State = BrokerState(Map(), Map(), Map(), Map())
  val socketIdMap = scala.collection.mutable.Map[Int, IdSocket]() //TODO: remove on socket close
  def pendingTransmissionsObservable(p: (IdSocket, Packet)): Observable[(IdSocket, Packet)] = p match {
    case (idSocket, packet) => Observable[(IdSocket, Packet)](s => {
      println(Thread.currentThread() + "    Handling: " + packet + " from " + idSocket.id)
      state = BrokerManager.handle(state, packet, MQTTChannel(idSocket.id))
      
      def sendAllPendingTransmissions(pendingTransmissions: Map[Channel, Seq[Packet]], closing: Boolean): Unit = {
        pendingTransmissions.foreach { case (channel, packets) =>
          packets.foreach(packet => {
            s.onNext((socketIdMap(channel.id), packet))
          })
          if (closing) {
            s.onNext((socketIdMap(channel.id), ClosePacket))
          }
        }
        
      }
      
      state.takeAllPendingTransmission match {
        case (newState, pendingTransmissions) =>
          state = newState
          sendAllPendingTransmissions(pendingTransmissions, closing = false)
      }
      
      state.takeClosing match {
        case (newState, pendingTransmissions) =>
          state = newState
          sendAllPendingTransmissions(pendingTransmissions, closing = true)
      }
    })
  }
  
  val errorStream: Subject[(IdSocket, Packet)] = PublishSubject[(IdSocket, Packet)]()
  
  
  clientStream(server).subscribeOn(acceptConnectionsScheduler)
    .flatMap(client => incomingPacketsObservable(client).subscribeOn(ioScheduler))
    .merge(errorStream).flatMap(packet => pendingTransmissionsObservable(packet).subscribeOn(packetHandlerScheduler))
    .groupBy(_._1.id).foreach(p => p._2.subscribeOn(ioScheduler).subscribe(Sender(errorStream)))
  
  
  case class Sender(errorStream: Subject[(IdSocket, Packet)]) extends Observer[(IdSocket, Packet)] {
    override def onNext(value: (IdSocket, Packet)): Unit = value match {
      case (idSocket, packet) => {
        implicit def packetToByteArray(p: Packet): Array[Byte] = MqttPacketBuilder.build(p).toBytes.toArray
        try {
          println(Thread.currentThread() + "    Sending: " + packet + " to " + idSocket.id)
          packet match {
            case ClosePacket =>
              idSocket.socket.close()
            case _ =>
              idSocket.socket.getOutputStream.write(packet)
              Thread.sleep(2000)
              idSocket.socket.getOutputStream.flush()
          }
        } catch {
          case _: Exception => {
            errorStream.onNext(idSocket, ???) //TODO: Add special wrapper packets
            idSocket.socket.close()
          }
        }
      }
    }
  }
}


class Client extends Thread {
  implicit def packetToByteArray(p: Packet): Array[Byte] = MqttPacketBuilder.build(p).toBytes.toArray
  override def run(): Unit = {
    val s = new Socket(InetAddress.getByName("localhost"), 9999)
    Thread.sleep(1000)
    val connect = Connect(Protocol("MQTT", 4), cleanSession = true, 10 second, Thread.currentThread().toString, Option.empty, Option.empty)
    s.getOutputStream.write(connect)
    s.getOutputStream.flush()
    Thread.sleep(10000)
    val disconnect = Disconnect()
    s.getOutputStream.write(disconnect)
    s.getOutputStream.flush()
    s.close()
  }
}