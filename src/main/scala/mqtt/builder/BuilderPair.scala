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
  override def build[R <: P](packet: R)(implicit context: Context[R]): Seq[Bit] =
    left.build(packet)(Context(Option(this))) ++ right.build(packet)(Context(Option(this)))
}
