package mqtt.builder


object BuildContext {
  /**
   * The build context.
   *
   * @param root if present, represent the root BuilderPair
   * @tparam P the type of the object needed to build
   */
  case class Context[P](root: Option[BuilderPair[P]])
  
  implicit def defaultBuildContext[P]: Context[P] = Context[P](Option.empty)
}