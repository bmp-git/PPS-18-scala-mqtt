package mqtt.server

import mqtt.model.Packet
import mqtt.model.Packet.ConnectReturnCode.ConnectionAccepted
import mqtt.model.Packet.{Connack, Disconnect}
import mqtt.server.AssertionsHelper._
import org.scalatest.FunSuite

class ConnectDisconnectTest extends FunSuite {
  test("A client should be able to connect") {
    val connect1 = basicConnect("c1")
    val connect2 = basicConnect("c2")
    val disconnect = Disconnect()
    val connack = Connack(sessionPresent = false, ConnectionAccepted)
    assertSendReceive(50000)(Seq[(Seq[Packet], Seq[Packet])](
      (Seq[Packet](connect1, disconnect), Seq[Packet](connack)),
      (Seq[Packet](connect2, disconnect), Seq[Packet](connack))
    ))
  }
}
