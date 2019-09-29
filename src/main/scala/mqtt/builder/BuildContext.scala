package mqtt.builder


object BuildContext {
  /**
   * The build context.
   *
   * @param parent if present, represent the parent BuilderPair
   * @tparam P the type of the object needed to build
   */
  case class Context[-P](parent: Option[BuilderPair[P]])
  
  implicit def defaultBuildContext[P]: Context[P] = Context[P](Option.empty)
}