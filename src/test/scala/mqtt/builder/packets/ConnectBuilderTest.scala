package mqtt.builder.packets

import mqtt.model.Packet.{ApplicationMessage, Connect, Credential, Protocol}
import mqtt.model.QoS
import mqtt.utils.Bit
import mqtt.utils.BitImplicits._

import scala.concurrent.duration._


class ConnectBuilderTest extends PacketBuilderTester {
  
  assertBuild(Map[Connect, Seq[Bit]](
    Connect(Protocol("MQTT", 4), cleanSession = false, 10 seconds, "a", Option.empty, Option.empty) ->
      Seq(
        0, 0, 0, 1, 0, 0, 0, 0, //id and reserved
        0, 0, 0, 0, 1, 1, 0, 1, //remaining length
        0, 0, 0, 0, 0, 0, 0, 0, //length MSB protocol name
        0, 0, 0, 0, 0, 1, 0, 0, //length LSB protocol name
        0, 1, 0, 0, 1, 1, 0, 1, //'M'
        0, 1, 0, 1, 0, 0, 0, 1, //'Q
        0, 1, 0, 1, 0, 1, 0, 0, //'T'
        0, 1, 0, 1, 0, 1, 0, 0, //'T'
        0, 0, 0, 0, 0, 1, 0, 0, //level 4
        0, 0, 0, 0, 0, 0, 0, 0, //flags
        0, 0, 0, 0, 0, 0, 0, 0, //keep alive MSB
        0, 0, 0, 0, 1, 0, 1, 0, //keep alive LSB
        0, 0, 0, 0, 0, 0, 0, 0, //length MSB client id
        0, 0, 0, 0, 0, 0, 0, 1, //length LSB client id
        0, 1, 1, 0, 0, 0, 0, 1, //'a'
      ),
    Connect(Protocol("MQTT", 4), cleanSession = true, 10 seconds, "a", Option(Credential("c", Option.empty)),
      Option(ApplicationMessage(retain = false, QoS(1), "b", Seq[Byte](10, 11)))) ->
      Seq(
        0, 0, 0, 1, 0, 0, 0, 0, //id and reserved
        0, 0, 0, 1, 0, 1, 1, 1, //remaining length
        0, 0, 0, 0, 0, 0, 0, 0, //length MSB protocol name
        0, 0, 0, 0, 0, 1, 0, 0, //length LSB protocol name
        0, 1, 0, 0, 1, 1, 0, 1, //'M'
        0, 1, 0, 1, 0, 0, 0, 1, //'Q
        0, 1, 0, 1, 0, 1, 0, 0, //'T'
        0, 1, 0, 1, 0, 1, 0, 0, //'T'
        0, 0, 0, 0, 0, 1, 0, 0, //level 4
        1, 0, 0, 0, 1, 1, 1, 0, //flags
        0, 0, 0, 0, 0, 0, 0, 0, //keep alive MSB
        0, 0, 0, 0, 1, 0, 1, 0, //keep alive LSB
        0, 0, 0, 0, 0, 0, 0, 0, //length MSB client id
        0, 0, 0, 0, 0, 0, 0, 1, //length LSB client id
        0, 1, 1, 0, 0, 0, 0, 1, //'a'
        0, 0, 0, 0, 0, 0, 0, 0, //length MSB will topic
        0, 0, 0, 0, 0, 0, 0, 1, //length LSB will topic
        0, 1, 1, 0, 0, 0, 1, 0, //'b'
        0, 0, 0, 0, 0, 0, 0, 0, //length MSB will payload
        0, 0, 0, 0, 0, 0, 1, 0, //length LSB will payload
        0, 0, 0, 0, 1, 0, 1, 0, //10
        0, 0, 0, 0, 1, 0, 1, 1, //11
        0, 0, 0, 0, 0, 0, 0, 0, //length MSB username
        0, 0, 0, 0, 0, 0, 0, 1, //length LSB username
        0, 1, 1, 0, 0, 0, 1, 1, //'c'
      ),
    Connect(Protocol("MQTT", 4), cleanSession = true, 10 seconds, "a", Option(Credential("c", Option(Seq[Byte](12)))),
      Option(ApplicationMessage(retain = true, QoS(2), "b", Seq[Byte](10, 11)))) ->
      Seq(
        0, 0, 0, 1, 0, 0, 0, 0, //id and reserved
        0, 0, 0, 1, 1, 0, 1, 0, //remaining length
        0, 0, 0, 0, 0, 0, 0, 0, //length MSB protocol name
        0, 0, 0, 0, 0, 1, 0, 0, //length LSB protocol name
        0, 1, 0, 0, 1, 1, 0, 1, //'M'
        0, 1, 0, 1, 0, 0, 0, 1, //'Q
        0, 1, 0, 1, 0, 1, 0, 0, //'T'
        0, 1, 0, 1, 0, 1, 0, 0, //'T'
        0, 0, 0, 0, 0, 1, 0, 0, //level 4
        1, 1, 1, 1, 0, 1, 1, 0, //flags
        0, 0, 0, 0, 0, 0, 0, 0, //keep alive MSB
        0, 0, 0, 0, 1, 0, 1, 0, //keep alive LSB
        0, 0, 0, 0, 0, 0, 0, 0, //length MSB client id
        0, 0, 0, 0, 0, 0, 0, 1, //length LSB client id
        0, 1, 1, 0, 0, 0, 0, 1, //'a'
        0, 0, 0, 0, 0, 0, 0, 0, //length MSB will topic
        0, 0, 0, 0, 0, 0, 0, 1, //length LSB will topic
        0, 1, 1, 0, 0, 0, 1, 0, //'b'
        0, 0, 0, 0, 0, 0, 0, 0, //length MSB will payload
        0, 0, 0, 0, 0, 0, 1, 0, //length LSB will payload
        0, 0, 0, 0, 1, 0, 1, 0, //10
        0, 0, 0, 0, 1, 0, 1, 1, //11
        0, 0, 0, 0, 0, 0, 0, 0, //length MSB username
        0, 0, 0, 0, 0, 0, 0, 1, //length LSB username
        0, 1, 1, 0, 0, 0, 1, 1, //'c'
        0, 0, 0, 0, 0, 0, 0, 0, //length MSB password
        0, 0, 0, 0, 0, 0, 0, 1, //length LSB password
        0, 0, 0, 0, 1, 1, 0, 0, //12
      )
  ))
}
