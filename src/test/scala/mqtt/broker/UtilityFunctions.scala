package mqtt.broker

import mqtt.broker.state.State
import mqtt.model.Packet
import mqtt.model.Types.ClientID
import org.scalatest.FunSuite

object UtilityFunctions extends FunSuite {
  def assertPacketPending(id: ClientID, f: Packet => Boolean): State => Unit = state => {
    state.sessionFromClientID(id).fold[Unit](fail)(s => {
      assert(s.pendingTransmission.exists(f))
    })
  }
  
  def assertPacketNotPending(id: ClientID, f: Packet => Boolean): State => Unit = state => {
    state.sessionFromClientID(id).fold[Unit](fail)(s => {
      assert(!s.pendingTransmission.exists(f))
    })
  }
  
  def assertPendingEmpty(id: ClientID): State => Unit = state => {
    state.sessionFromClientID(id).fold[Unit](fail)(s => {
      assert(s.pendingTransmission.isEmpty)
    })
  }
  
  def assertDisconnected(id: ClientID): State => Unit = state => {
    state.sessionFromClientID(id).fold[Unit](())(s => {
      assert(s.channel.isEmpty)
    })
  }
}
