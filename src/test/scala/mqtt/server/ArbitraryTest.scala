package mqtt.server

import mqtt.model.Packet
import mqtt.model.Packet.Disconnect
import mqtt.server.AssertionsHelper._
import mqtt.server.client.ArbitraryClient
import mqtt.utils.Bit
import mqtt.utils.BitImplicits._
import org.scalatest.FunSuite

class ArbitraryTest extends FunSuite {
  
  test("A client who send disconnect as first packet shouldn't damage the broker") {
    val connect = basicConnect("c1")
    val disconnect = Disconnect()
    assertSendReceive(50001)(Seq[(Seq[Packet], Seq[Packet])](
      (Seq[Packet](disconnect, connect), Seq[Packet]()),
    ))
  }
  
  test("A client who closes the connection early shouldn't damage the broker") {
    assertSendReceive(50002)(Seq[(Seq[Packet], Seq[Packet])](
      (Seq[Packet](), Seq[Packet]()),
    ))
  }
  
  test("A client who send a ad-hoc malformed packet shouldn't damage the broker") {
    val breaker = MqttBroker(50003).run()
    val client1 = ArbitraryClient(50003, Seq[Bit](0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)) //Not completed remaining length
    val client2 = ArbitraryClient(50003, Seq[Bit](0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0)) //Not matching remaining length
    val client3 = ArbitraryClient(50003, Seq[Bit](0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)) //First byte 0
    
    client1.start()
    client2.start()
    client3.start()
    
    client1.join()
    client2.join()
    client3.join()
    
    breaker.stop()
  }
}
