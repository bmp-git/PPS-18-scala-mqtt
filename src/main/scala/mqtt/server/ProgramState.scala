package mqtt.server


import mqtt.broker.state.{BrokerState, State}
import mqtt.model.BrokerConfig

import scala.collection.concurrent.TrieMap

/**
 * Encapsulates all side effect that need to be handled.
 * The state of the broker and the active socket.
 */
case class ProgramState() {
  private var _brokerState: State = BrokerState(Map(), Map(), Map(), Map(), Map(), BrokerConfig())
  private val socketMap = TrieMap[Int, IdSocket]()
  
  /**
   * Add a new socket to the register.
   *
   * @param idSocket the socket
   */
  def addSocket(idSocket: IdSocket): Unit = socketMap += (idSocket.id -> idSocket)
  
  /**
   * Remove the socket from the register.
   *
   * @param id the socket's id
   */
  def removeSocket(id: Int): Unit = socketMap -= id
  
  /**
   * Get a socket from the register.
   *
   * @param id the socket's id
   * @return the corresponding socket.
   */
  def socket(id: Int): IdSocket = socketMap(id)
  
  /**
   * Gets the broker state.
   *
   * @return the broker state
   */
  def brokerState: State = _brokerState
  
  /**
   * Sets the broker state.
   *
   * @param state the new broker state
   */
  def brokerState_=(state: State): Unit = _brokerState = state
}
