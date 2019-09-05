package mqtt.broker

trait Channel[T[_], K] {
  def send(m: T[K])
}

case class Socket(id: Int) extends Channel[Seq, Byte] {
  override def send(m: Seq[Byte]): Unit = ()
}
