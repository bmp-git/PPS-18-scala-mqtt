package mqtt.builder.packets

import mqtt.builder.MqttPacketBuilder
import mqtt.model.Packet.Connack
import mqtt.model.Packet.ConnectReturnCode._
import mqtt.utils.Bit
import org.scalatest.FunSuite
import mqtt.utils.BitImplicits._


class ConnackBuilderTest extends FunSuite {
  
  Map[Connack, Seq[Bit]](
    Connack(sessionPresent = false, returnCode = ConnectionAccepted) ->
      Seq(
        0, 0, 1, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 1, 0,
        0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0
      ),
    Connack(sessionPresent = true, returnCode = ConnectionAccepted) ->
      Seq(
        0, 0, 1, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 1, 0,
        0, 0, 0, 0, 0, 0, 0, 1,
        0, 0, 0, 0, 0, 0, 0, 0
      ),
    Connack(sessionPresent = true, returnCode = UnacceptableProtocolVersion) ->
      Seq(
        0, 0, 1, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 1, 0,
        0, 0, 0, 0, 0, 0, 0, 1,
        0, 0, 0, 0, 0, 0, 0, 1
      ),
    Connack(sessionPresent = true, returnCode = IdentifierRejected) ->
      Seq(
        0, 0, 1, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 1, 0,
        0, 0, 0, 0, 0, 0, 0, 1,
        0, 0, 0, 0, 0, 0, 1, 0
      ),
    Connack(sessionPresent = true, returnCode = ServerUnavailable) ->
      Seq(
        0, 0, 1, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 1, 0,
        0, 0, 0, 0, 0, 0, 0, 1,
        0, 0, 0, 0, 0, 0, 1, 1
      ),
    Connack(sessionPresent = true, returnCode = BadUsernameOrPassword) ->
      Seq(
        0, 0, 1, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 1, 0,
        0, 0, 0, 0, 0, 0, 0, 1,
        0, 0, 0, 0, 0, 1, 0, 0
      ),
    Connack(sessionPresent = true, returnCode = NotAuthorized) ->
      Seq(
        0, 0, 1, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 1, 0,
        0, 0, 0, 0, 0, 0, 0, 1,
        0, 0, 0, 0, 0, 1, 0, 1
      )) foreach {
    case (connack, bits) => {
      val build = MqttPacketBuilder.build(connack)
      val buildString = build.toBinaryString
      test(s"$connack should be builded in $buildString") {
        assert(build == bits)
      }
    }
  }
}
