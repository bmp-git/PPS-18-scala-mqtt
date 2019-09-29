package mqtt.builder

import mqtt.builder.BuildContext.Context
import mqtt.utils.Bit

/**
 * Abstraction for a builder that do not need external information in order to build.
 */
trait StaticBuilder extends Builder[Any] {
  override def build[R <: Any](packet: R)(implicit context: Context[R]): Seq[Bit] = build()
  
  /**
   * Defines the static definition of this builder.
   *
   * @return the static sequence of bits
   */
  def build(): Seq[Bit]
  
  /**
   * Chain two StaticBuilder and return a new one
   *
   * @param staticBuilder the StaticBuilder to chain
   * @return a new StaticBuilder
   */
  def ::(staticBuilder: StaticBuilder): StaticBuilder = () => staticBuilder.build() ++ this.build()
}
