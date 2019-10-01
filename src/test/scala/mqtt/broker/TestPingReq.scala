package mqtt.broker

import mqtt.broker.SampleInstances._
import mqtt.broker.UtilityFunctions._
import mqtt.broker.state.{Channel, State}
import mqtt.model.Packet.{Pingreq, Pingresp}
import org.scalatest.FunSuite

trait TestPingReq extends FunSuite {
  def PingReqHandler: (State, Pingreq, Channel) => State
  
  test("A PINGREQ should respond with PINGRESP.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_0.copy(channel = Option(sample_channel_0)))
    val bs2 = PingReqHandler(bs1, Pingreq(), sample_channel_0)
    assertPacketPending(sample_id_0, {
      case _: Pingresp => true
      case _ => false
    })(bs2)
  }
  
  test("A PINGREQ should update last contact.") {
    val bs1 = bs0.setSession(sample_id_0, sample_session_0.copy(channel = Option(sample_channel_0)))
    val bs2 = PingReqHandler(bs1, Pingreq(), sample_channel_0)
    bs2.sessionFromClientID(sample_id_0).fold(fail)(s => assert(s.lastContact.getTime > sample_session_0.lastContact.getTime))
  }
}
