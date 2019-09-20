package mqtt.server

import java.net.ServerSocket

import mqtt.utils.IterableImplicits._
import rx.lang.scala.Observable

//TODO: doc
object Listener {
  
  //TODO: doc
  case class SocketIdGenerator(count: Int) {
    def next(): (Int, SocketIdGenerator) = {
      (count, SocketIdGenerator(count + 1))
    }
  }
  
  //TODO: doc
  def apply(server: ServerSocket, socketIdMap: scala.collection.mutable.Map[Int, IdSocket]): Observable[IdSocket] =
    Observable[IdSocket](s => {
      Stream.continually(server.accept()).bendLeft(SocketIdGenerator(0))((generator, socket) => {
        val next = generator.next()
        val idSocket = IdSocket(next._1, socket, closed = false)
        socketIdMap += (next._1 -> idSocket)
        println(Thread.currentThread() + "    New client: " + idSocket.id)
        s.onNext(idSocket)
        next._2
      })
    })
}
