package mqtt.broker

import mqtt.broker.state.{Channel, State}
import mqtt.model.Packet
import mqtt.model.Packet.Connack
import mqtt.model.Packet.ConnectReturnCode.{ConnectionAccepted, NotAuthorized}
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
  
  def assertClosing(channel: Channel): State => Unit = state => {
    assert(state.closing.get(channel).isDefined)
  }
  
  def assertClosingWithPacket(channel: Channel, f: Packet => Boolean): State => Unit = state => {
    state.closing.get(channel).fold(fail)(seq => {
      assert(seq.exists(f))
    })
  }
  
  def assertConnectionAccepted(id: ClientID): State => Unit = state => {
    assert {
      state.sessionFromClientID(id).fold(fail)(s => {
        s.pendingTransmission.find {
          case Connack(_, ConnectionAccepted) => true
          case _ => false
        }
      }).isDefined
    }
  }
  
  def assertNotAuthorized(channel: Channel): State => Unit =
    assertClosingWithPacket(channel, {
      case Connack(_, NotAuthorized) => true
      case _ => false
    })
}
