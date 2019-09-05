package mqtt.builder

import mqtt.builder.fragments.PacketFragmentList


package object buildContext {
  case class Context[-P](parent: Option[PacketFragmentList[P]])
  
  implicit def defaultBuildContext[P]: Context[P] = Context[P](Option.empty)
}