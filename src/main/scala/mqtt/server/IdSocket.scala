package mqtt.server

import java.net.Socket

/**
 * Add id attribute to a socket.
 *
 * @param id     the unique id
 * @param socket the socket
 */
case class IdSocket(id: Int, socket: Socket)
