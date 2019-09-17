import java.net.{ServerSocket, Socket, _}
import java.util.concurrent.Executors

import mqtt.broker.{BrokerManager, BrokerState, State}
import mqtt.builder.MqttPacketBuilder
import mqtt.model.Packet
import mqtt.model.Packet.{Connect, Disconnect, Protocol}
import mqtt.parser.MqttPacketParser
import mqtt.utils.BitImplicits._
import rx.lang.scala.schedulers._
import rx.lang.scala.subjects.PublishSubject
import rx.lang.scala.{Observable, Observer}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object RxMain extends App {
  
  val server = new ServerSocket(9999)
  
  val connectionsExecutor = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  val connectionsScheduler = ExecutionContextScheduler(connectionsExecutor)
  
  val packetHandlerExecutor = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
  val packetHandlerScheduler = ExecutionContextScheduler(packetHandlerExecutor)
  
  (0 to 5).foreach(_ => {
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
  
  val socketIdMap = scala.collection.mutable.Map[Int, IdSocket]() //TODO: remove on socket close
  
  val clientStream = Observable[IdSocket](s => {
    Stream.continually(server.accept()).foreach(socket => {
      val id = SocketIdGenerator.next()
      val idSocket = IdSocket(id, socket)
      socketIdMap += (id -> idSocket)
      s.onNext(idSocket)
    })
  });
  
  val errorStream = PublishSubject[(IdSocket, Packet)]()
  
  val packetsStream = clientStream.observeOn(connectionsScheduler).flatMap(idSocket => Observable[(IdSocket, Packet)](s => {
    println("New client! " + Thread.currentThread())
    val in = idSocket.socket.getInputStream
    //TODO: refactor this abomination
    var go = true
    while (go) {
      try {
        val buffer = new Array[Byte](2)
        in.read(buffer)
        val buffer1 = new Array[Byte](buffer(1)) //TODO: fix with variable length integer (parser too)
        in.read(buffer1)
        val bits = (buffer.toSeq ++ buffer1.toSeq).toBitsSeq
        val receivedPacket = MqttPacketParser.parse(bits)
        s.onNext((idSocket, receivedPacket))
        if (receivedPacket.isInstanceOf[Disconnect]) {
          go = false
          idSocket.socket.close()
          s.onCompleted()
        }
        println(receivedPacket)
      } catch {
        case ex: Throwable => {
          //TODO: what to do on error?
          println(ex.getMessage)
          s.onCompleted()
          //s.onError(ex)
          idSocket.socket.close()
          go = false
        }
      }
    }
  }))
  
  var state: State = BrokerState(Map(), Map(), Map(), Map())
  
  
  val toSendStream = packetsStream.merge(errorStream).observeOn(packetHandlerScheduler)
    .flatMap { case (idSocket, packet) => Observable[(IdSocket, Packet)](s => {
    println("handling " + packet + " " + Thread.currentThread())
    
    state = BrokerManager.handle(state, packet, mqtt.broker.MQTTChannel(idSocket.id))
    state.takeAllPendingTransmission match {
      case (newState, pendingTransmissions) =>
        state = newState
        pendingTransmissions.foreach { case (channel, packets) =>
          packets.foreach(packet => {
            s.onNext((socketIdMap(channel.id), packet))
          })
        }
    }
    
  })
  }
  
  
  toSendStream.observeOn(connectionsScheduler).subscribe(new Asd())
  
  class Asd extends Observer[(IdSocket, Packet)] {
    override def onNext(value: (IdSocket, Packet)): Unit = value match {
      case (idSocket, packet) => {
        implicit def packetToByteArray(p: Packet): Array[Byte] = MqttPacketBuilder.build(p).toBytes.toArray
        
        try {
          println("sending " + packet + " to " + idSocket.id + " " + Thread.currentThread())
          idSocket.socket.getOutputStream.write(packet)
          Thread.sleep(1000)
          idSocket.socket.getOutputStream.flush()
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
    val connect = Connect(Protocol("MQTT", 4), true, 10 second, Thread.currentThread().toString, Option.empty, Option.empty)
    s.getOutputStream.write(connect)
    s.getOutputStream.flush()
    Thread.sleep(10000)
    val disconnect = Disconnect()
    s.getOutputStream.write(disconnect)
    s.getOutputStream.flush()
    s.close()
  }
}