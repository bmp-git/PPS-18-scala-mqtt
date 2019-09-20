package mqtt.server

import java.net.Socket

//TODO: doc
case class IdSocket(id: Int, socket: Socket, closed: Boolean)
