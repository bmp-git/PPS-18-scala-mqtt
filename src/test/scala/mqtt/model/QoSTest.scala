package mqtt.model

import org.scalatest.{FunSuite, Matchers}

import scala.util.Try

class QoSTest extends FunSuite with Matchers {
  test("Maximum qos should be 2") {
    QoS(0)
    QoS(1)
    QoS(2)
    assert(Try {
      QoS(3)
    }.toOption.isEmpty)
  }
  
  test("QoS unapply should give the qos value") {
    (0 to 2) foreach { v =>
      QoS(v) match {
        case QoS(value) => value shouldBe v
      }
    }
  }
}
