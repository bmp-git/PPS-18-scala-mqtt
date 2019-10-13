package mqtt.broker

import mqtt.broker.SampleInstances._
import mqtt.model.QoS
import org.scalatest.FunSuite

class TestUtils extends FunSuite {
  test("QoS1 is less than QoS2.") {
    assert(Common.min(QoS(1), QoS(2)) == QoS(1))
  }
  
  
  test("The sha256 function should compute the correct output string.") {
    val msg = sample_password_0
    val bytes = msg.getBytes("UTF-8")
    val digest = Common.sha256(bytes)
    assert(digest == sample_password_0_digest)
  }
}
