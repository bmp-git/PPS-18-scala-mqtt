package mqtt.server

import java.net.ServerSocket

import com.typesafe.scalalogging.LazyLogging
import mqtt.utils.IdGenerator
import mqtt.utils.IterableImplicits._
import rx.lang.scala.Observable

import scala.util.Try

/**
 * Contains the listen logic.
 * Interfaces with socket listener and accept clients.
 */
object Listener extends LazyLogging {
  /**
   * Listens for incoming connection and generate a source of clients.
   * When the server is closed or an exception occur the stream will complete.
   *
   * @param server the listener
   * @return the source of IdSockets (unique id, socket)
   */
  def apply(server: ServerSocket): Observable[IdSocket] =
    Observable[IdSocket](s => {
      Try {
        Stream.continually(server.accept())
          .bendLeft(IdGenerator(0))((generator, socket) => {
            generator.next() match {
              case (id, idGenerator) =>
                val idSocket = IdSocket(id, socket)
                logger.debug(s"New client: ${idSocket.id}")
                s.onNext(idSocket)
                idGenerator
            }
          })
      } recover {
        case _: Throwable => s.onCompleted()
      }
    })
}
