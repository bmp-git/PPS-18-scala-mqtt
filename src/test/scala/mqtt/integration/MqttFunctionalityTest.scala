package mqtt.integration

import org.eclipse.paho.client.mqttv3._
import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}
import scala.util.Try

/**
 * Test of MQTT functionality using Paho MQTT client library.
 */
class MqttFunctionalityTest extends MqttClientServerTest("localhost", 1000) {
  val defaultTopic = "a/b"
  val defaultFilter = "a/#"
  
  val defaultMessage = "That's a good message."
  
  test("A real MQTTClient should publish a message with QoS0") {
    client.publish(defaultTopic, defaultMessage.getBytes(), 0, false)
    succeed
  }
  
  test("A real MQTTClient should subscribe to a topic with QoS0") {
    val res = client.subscribeWithResponse(defaultTopic, 0)
    assert(res isComplete)
    res.getGrantedQos.foreach(_ shouldBe 0)
  }
  
  test("A real MQTTClient should subscribe to more topics") {
    val res = client.subscribeWithResponse(Seq(defaultTopic, "foo/bar").toArray, Seq(0,0).toArray)
    assert(res isComplete)
    res.getGrantedQos.foreach(_ shouldBe 0)
  }
  
  test("As a client, I want to publish a message in a topic and subscribe to a topic") {
    val result: Promise[Boolean] = Promise()
    client.subscribe(defaultFilter, 0)
    
    //Removed when client disconnects
    client.setCallback(new MqttCallback {
      override def connectionLost(cause: Throwable): Unit = {}
      
      override def messageArrived(topic: String, message: MqttMessage): Unit = {
        val _ = result.success(new String(message.getPayload) == defaultMessage && topic == defaultTopic)
      }
      
      override def deliveryComplete(token: IMqttDeliveryToken): Unit = {}
    })
    
    client.publish(defaultTopic, defaultMessage.getBytes(), 0, false)
    
    assert(Try {
      Await.result(result future, 1 seconds)
    } getOrElse false)
  }
  
}
