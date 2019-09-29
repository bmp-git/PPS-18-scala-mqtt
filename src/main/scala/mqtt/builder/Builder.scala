package mqtt.builder

import mqtt.builder.BuildContext._
import mqtt.utils.Bit

/**
 * Defines an abstraction for a builder. It can be chained to other builders and can be built into
 * a sequence of bits.
 *
 * @tparam P the type of the object needed to build
 */
trait Builder[-P] {
  /**
   * Chain two builders by merging them into one.
   *
   * @param builder the builder to be chained
   * @tparam R the type of the object needed to build
   * @return a new builder representing the concatenation of two
   */
  final def ::[R <: P](builder: Builder[R]): Builder[R] = builder match {
    case pfp: BuilderPair[P] => pfp.left :: pfp.right :: this //unpack pairs to respect (x,(x,(x,...))) structure
    case _ => BuilderPair(builder, this)
  }
  
  /**
   * Builds the input in the correspondent sequence of bits.
   *
   * @param value   the object needed for the build process
   * @param context the build context
   * @tparam R the type of the object needed to build
   * @return the bits sequence that this builder built with the given input
   */
  def build[R <: P](value: R)(implicit context: Context[R]): Seq[Bit]
}