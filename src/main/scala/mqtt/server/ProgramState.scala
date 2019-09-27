package mqtt.server

import mqtt.broker.{BrokerState, State}

import scala.collection.concurrent.TrieMap

case class ProgramState() {
  private var _brokerState: State = BrokerState(Map(), Map(), Map(), Map())
  private val socketMap = TrieMap[Int, IdSocket]()
  
  def addSocket(idSocket: IdSocket): Unit = socketMap += (idSocket.id -> idSocket)
  
  def removeSocket(id: Int): Unit = socketMap -= id
  
  def socket(id: Int): IdSocket = socketMap(id)
  
  def brokerState: State = _brokerState
  
  def brokerState_=(state: State): Unit = _brokerState = state
}
