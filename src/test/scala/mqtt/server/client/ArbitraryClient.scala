package mqtt.server.client

import java.net.Socket

import mqtt.utils.Bit
import mqtt.utils.BitImplicits._

/**
 * A mqtt client that send the wanted raw bits.
 *
 * @param port   the connection's port
 * @param toSend the bits to send
 */
case class ArbitraryClient(port: Int, toSend: Seq[Bit]) extends BaseClient(port) {
  override protected def execute(socket: Socket): Unit = {
    socket.getOutputStream.write(toSend.toBytes.toArray)
    socket.getOutputStream.flush()
  }
}
