package mqtt.broker

trait Channel[T[_], K] {
  def send(m: T[K])
}
