package mqtt.client

import mqtt.utils.Bit
import mqtt.utils.BitImplicits._

case class ArbitraryClient(port: Int, toSend: Seq[Bit]) extends BaseClient(port) {
  
  override def run(): Unit = {
    val socket = init()
    try {
      socket.getOutputStream.write(toSend.toBytes.toArray)
      socket.getOutputStream.flush()
      Thread.sleep(500) //give time to flush
      socket.close()
    } catch {
      case _: Exception =>
    }
  }
  
}
