package mqtt.builder

import mqtt.builder.BuildContext.Context
import mqtt.utils.Bit


/**
 * Data structure used to chain builders.
 * Example a :: b
 *
 * @param left  the left chained builder (a)
 * @param right the right chained builder (b)
 * @tparam P the type of the object needed to build
 */
case class BuilderPair[-P](left: Builder[P], right: Builder[P]) extends Builder[P] {
  override def build[R <: P](packet: R)(implicit context: Context[R]): Seq[Bit] = {
    val newContext: Context[R] = context.root match {
      case None => Context(Option(this))
      case Some(_) => context
    }
    left.build(packet)(newContext) ++ right.build(packet)(newContext)
  }
  
}
