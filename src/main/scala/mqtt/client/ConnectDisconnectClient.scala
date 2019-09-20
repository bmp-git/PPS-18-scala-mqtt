package mqtt.client

import java.net.{InetAddress, Socket}

import mqtt.builder.MqttPacketBuilder
import mqtt.model.Packet
import mqtt.model.Packet.{Connect, Disconnect}
import scala.concurrent.duration._
import mqtt.utils.BitImplicits._

//TODO: doc, refactor
class ConnectDisconnectClient extends Thread {
  implicit def packetToByteArray(p: Packet): Array[Byte] = MqttPacketBuilder.build(p).toBytes.toArray
  
  override def run(): Unit = {
    try {
      val s = new Socket(InetAddress.getByName("localhost"), 9999)
      Thread.sleep(1000)
      val connect = Connect(mqtt.model.Packet.Protocol("MQTT", 4), cleanSession = true, 10 second,
        Thread.currentThread().toString.replace("Thread", ""), Option.empty, Option.empty)
      s.getOutputStream.write(connect)
      s.getOutputStream.flush()
      Thread.sleep(5000)
      val disconnect = Disconnect()
      s.getOutputStream.write(disconnect)
      s.getOutputStream.flush()
      Thread.sleep(1000)
      s.close()
    } catch {
      case _: Exception => {}
    }
  }
  
}
