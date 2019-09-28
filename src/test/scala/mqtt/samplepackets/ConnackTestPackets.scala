package mqtt.samplepackets

import mqtt.model.Packet
import mqtt.model.Packet.Connack
import mqtt.model.Packet.ConnectReturnCode._
import mqtt.utils.Bit
import mqtt.utils.BitImplicits._

object ConnackTestPackets {
  val samples: Map[Packet, Seq[Bit]] = Map(
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
      ))
}
