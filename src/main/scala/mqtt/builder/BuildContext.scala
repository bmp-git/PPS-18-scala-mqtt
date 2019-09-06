package mqtt.builder

import mqtt.builder.fragments.PacketFragmentPair


object BuildContext {
  /**
   * The context of packet fragments build.
   * @param parent if present, represent the parent PacketFragmentPair
   * @tparam P the type of the object that the parent refers to
   */
  case class Context[-P](parent: Option[PacketFragmentPair[P]])
  
  implicit def defaultBuildContext[P]: Context[P] = Context[P](Option.empty)
}