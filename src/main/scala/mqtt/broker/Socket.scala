package mqtt.broker

//TODO this is a draft to be refined

/**
 * Represents a communication channel related to a client.
 *
 * @param id          the id to identify the socket
 */
case class Socket(id: Int) extends Channel[Seq, Byte] {
  override def send(m: Seq[Byte]): Unit = ()
}
