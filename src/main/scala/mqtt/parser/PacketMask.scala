package mqtt.parser

import mqtt.utils.Bit
import mqtt.utils.BitImplicits._

/**
 * The 4 bit mask that represent the MQTT packet type.
 */
trait PacketMask {
  val code: Seq[Bit]
}

case object ConnectMask extends PacketMask {
  override val code: Seq[Bit] = Seq(0, 0, 0, 1)
}

case object ConnackMask extends PacketMask {
  override val code: Seq[Bit] = Seq(0, 0, 1, 0)
}

case object PublishMask extends PacketMask {
  override val code: Seq[Bit] = Seq(0, 0, 1, 1)
}

case object PubackMask extends PacketMask {
  override val code: Seq[Bit] = Seq(0, 1, 0, 0)
}

case object PubrecMask extends PacketMask {
  override val code: Seq[Bit] = Seq(0, 1, 0, 1)
}

case object PubrelMask extends PacketMask {
  override val code: Seq[Bit] = Seq(0, 1, 1, 0)
}

case object PubcompMask extends PacketMask {
  override val code: Seq[Bit] = Seq(0, 1, 1, 1)
}

case object SubscribeMask extends PacketMask {
  override val code: Seq[Bit] = Seq(1, 0, 0, 0)
}

case object SubackMask extends PacketMask {
  override val code: Seq[Bit] = Seq(1, 0, 0, 1)
}

case object UnsubscribeMask extends PacketMask {
  override val code: Seq[Bit] = Seq(1, 0, 1, 0)
}

case object UnsubackMask extends PacketMask {
  override val code: Seq[Bit] = Seq(1, 0, 1, 1)
}

case object PingreqMask extends PacketMask {
  override val code: Seq[Bit] = Seq(1, 1, 0, 0)
}

case object PingrespMask extends PacketMask {
  override val code: Seq[Bit] = Seq(1, 1, 0, 1)
}

case object DisconnectMask extends PacketMask {
  override val code: Seq[Bit] = Seq(1, 1, 1, 0)
}

