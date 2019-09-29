package mqtt.server.client

import java.net.Socket

import mqtt.builder.MqttPacketBuilder
import mqtt.model.Packet
import mqtt.model.Packet.Disconnect
import mqtt.utils.BitImplicits._

/**
 * A mqtt client that send the wanted packets in the specified sequence.
 *
 * @param port    the connection's port
 * @param packets the packets to send
 */
case class DummyClient(port: Int, packets: Seq[Packet]) extends BaseClient(port) {
  implicit def packetToByteArray(p: Packet): Array[Byte] = MqttPacketBuilder.build(p).toBytes.toArray
  
  override protected def execute(socket: Socket): Unit = {
    packets.foreach { packet =>
      packet match {
        case Disconnect() => waitIO() //prevent server to instant-close the connection
        case _ => ()
      }
      socket.getOutputStream.write(packet)
      socket.getOutputStream.flush()
    }
  }
}
