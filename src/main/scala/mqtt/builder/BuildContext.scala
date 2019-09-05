package mqtt.builder

import mqtt.builder.fragments.PacketFragmentList


object BuildContext {
  case class Context[-P](parent: Option[PacketFragmentList[P]])
  
  implicit def defaultBuildContext[P]: Context[P] = Context[P](Option.empty)
}