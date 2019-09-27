package mqtt.server

import java.net.ServerSocket

import mqtt.utils.IdGenerator
import mqtt.utils.IterableImplicits._
import rx.lang.scala.Observable

/**
 * Contains the listen logic.
 * Interfaces with socket listener and accept clients.
 */
object Listener {
  /**
   * Listen for incoming connection and generate a source of clients.
   *
   * @param server the listener
   * @return the source of IdSockets (unique id, socket)
   */
  def apply(server: ServerSocket): Observable[IdSocket] =
    Observable[IdSocket](s => {
      Stream.continually(server.accept())
        .bendLeft(IdGenerator(0))((generator, socket) => {
          generator.next() match {
            case (id, idGenerator) =>
              val idSocket = IdSocket(id, socket, closed = false)
              println(Thread.currentThread() + "    New client: " + idSocket.id)
              s.onNext(idSocket)
              idGenerator
          }
      })
    })
}
