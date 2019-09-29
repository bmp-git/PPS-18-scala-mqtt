import mqtt.server._

/**
 * Entry point of the mqtt broker.
 * By default it listens at port 9999.
 */
object RxMain extends App {
  val stopper = MqttBroker(9999).run()
  scala.io.StdIn.readLine()
  stopper.stop()
  println("Bye")
}


