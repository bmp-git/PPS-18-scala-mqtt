import java.net.ServerSocket
import java.util.concurrent.Executors

import mqtt.broker._
import mqtt.client.ConnectDisconnectClient
import mqtt.model.Packet
import mqtt.server.{IdSocket, Sender, _}
import rx.lang.scala.Observable
import rx.lang.scala.schedulers._

import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

//TODO: doc, refactor
object RxMain extends App {
  
  //TODO: use a logger!
  
  val server = new ServerSocket(9999)
  
  val ioScheduler = IOScheduler()
  
  val packetHandlerExecutor = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
  val packetHandlerScheduler = ExecutionContextScheduler(packetHandlerExecutor)
  
  (0 until 3).foreach(_ => {
    new ConnectDisconnectClient().start()
  })
  
  var state: State = BrokerState(Map(), Map(), Map(), Map())
  
  val socketIdMap = TrieMap[Int, IdSocket]()
  
  val tickStream: Observable[Option[Nothing]] = Observable.interval(1 second).map(_ => Option.empty).subscribeOn(ioScheduler)
  
  //TODO: groupBy has a problem until at least 2 different id are handled
  Listener(server, socketIdMap)
    .flatMap(client => Receiver(client).subscribeOn(ioScheduler))
    .map(Option[(IdSocket, Packet)])
    .merge(tickStream)
    .flatMap(packet => ProtocolHandler(packet, state, s => state = s, socketIdMap).subscribeOn(packetHandlerScheduler))
    .groupBy { case (socket, _) => socket.id }
    .map {
      case (socketId, toSend) =>
        toSend.map { case (_, packet) => packet }.takeUntil(_ match {
          case `ClosePacket` => true
          case _ => false
        }).subscribeOn(ioScheduler).subscribe(Sender(socketId, socketIdMap))
    }.foreach(_ => {})
}


