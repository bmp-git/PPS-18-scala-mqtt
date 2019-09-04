package mqtt.broker

import mqtt.Socket
import mqtt.model.Packet
import mqtt.model.Packet.ApplicationMessage
import mqtt.model.Types.{ClientID, Topic}

trait State {
  def sessions: Map[ClientID, Session]
  
  def retains: Map[Topic, ApplicationMessage]
  
  def closing: Map[Socket, Seq[Packet]]
  
  def addClosingChannel(socket: Socket, packets: Seq[Packet]): State
  
  def sessionFromClientID(clientID: ClientID): Option[Session]
  
  def sessionFromSocket(socket: Socket): Option[Session]
  
  def setSession(clientID: ClientID, session: Session): State
  
  def setSocket(clientID: ClientID, socket: Socket): State
}
