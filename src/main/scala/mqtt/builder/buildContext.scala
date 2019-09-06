package mqtt.builder

import mqtt.builder.fragments.{PacketFragmentPair}


package object buildContext {
  case class Context[-P](parent: Option[PacketFragmentPair[P]])
  
  implicit def defaultBuildContext[P]: Context[P] = Context[P](Option.empty)
}