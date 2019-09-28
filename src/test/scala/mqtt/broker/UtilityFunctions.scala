package mqtt.broker

import mqtt.broker.state.State
import mqtt.model.Packet
import mqtt.model.Types.ClientID
import org.scalatest.{Assertion, FunSuite}

object UtilityFunctions extends FunSuite {
  def assertPacketPending(id: ClientID, f: Packet => Boolean): State => Assertion = state => {
    state.sessionFromClientID(id).fold(fail)(s => {
      s.pendingTransmission.find(f).fold(fail)(_ => succeed)
    })
  }
  
  def assertPacketNotPending(id: ClientID, f: Packet => Boolean): State => Assertion = state => {
    state.sessionFromClientID(id).fold(fail)(s => {
      s.pendingTransmission.find(f).fold(succeed)(_ => fail)
    })
  }
  
  def assertPendingEmpty(id: ClientID): State => Assertion = state => {
    state.sessionFromClientID(id).fold(fail)(s => {
      assert(s.pendingTransmission.isEmpty)
    })
  }
  
  def assertDisconnected(id: ClientID): State => Assertion = state => {
    state.sessionFromClientID(id).fold(succeed)(s => {
      assert(s.channel.isEmpty)
    })
  }
}
