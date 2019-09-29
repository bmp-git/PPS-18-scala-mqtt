package mqtt.client

import mqtt.builder.MqttPacketBuilder
import mqtt.model.Packet
import mqtt.model.Packet.Disconnect
import mqtt.utils.BitImplicits._

case class DummyClient(port: Int, actions: Seq[Packet]) extends BaseClient(port) {
  implicit def packetToByteArray(p: Packet): Array[Byte] = MqttPacketBuilder.build(p).toBytes.toArray

  override def run(): Unit = {
    val socket = init()
    
    try {
      actions.foreach { packet =>
        packet match {
          case Disconnect() => Thread.sleep(500) //give time to flush remaining bytes
          case _ =>
        }
        socket.getOutputStream.write(packet)
        socket.getOutputStream.flush()
      }
      socket.close()
    } catch {
      case _: Exception =>
    }
  }
  
}
