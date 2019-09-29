package mqtt.builder

import mqtt.builder.BuildContext.Context
import mqtt.utils.Bit

/**
 * Include an explicit extension for builder.
 * Enables a simple dsl for builder composition.
 */
object RichBuilder {
  
  /**
   * Extends the Builder trait in order to provide a simple dsl for builder composition.
   *
   * @param builder the instance to extend
   * @tparam T the type of the object needed to build
   */
  implicit class RichBuilderExtension[T](builder: Builder[T]) {
    /**
     * Transforms this builder from type T to P.
     * In the building process this new builder will require a P value.
     * Example: (stringBuilder from ((v: MyStructure) => v.myString)) build (myStructure)
     *
     * @param ex the extractor, selects a T from a P
     * @tparam P the new type of the object needed to build
     * @return the new builder
     */
    def from[P](ex: P => T): Builder[P] = new Builder[P] {
      override def build[R <: P](packet: R)(implicit context: Context[R]): Seq[Bit] = builder.build(ex(packet))
    }
    
    /**
     * Transforms this builder from type T to P.
     * In the building process this new builder will require a P value and
     * will builds foreach element flatMapping the results.
     * Example: (stringBuilder foreach ((v: MyStructure) = v.myStrings) build (myStructure)
     *
     * @param ex the extractor, select a T collection from a P
     * @tparam P the new type of the object needed to build
     * @return the new builder
     */
    def foreach[P](ex: P => Seq[T]): Builder[P] = new Builder[P] {
      override def build[R <: P](packet: R)(implicit context: Context[R]): Seq[Bit] =
        ex(packet).flatMap(v => builder.build(v))
    }
    
    /**
     * Builds this builder with the given data and save the result in order
     * creating a new StaticBuilder.
     *
     * @param data the build input
     * @tparam P the type of the object needed to build
     * @return a new StaticBuilder representing the result of the build
     */
    def of[P <: T](data: P): StaticBuilder = () => builder.build(data)
  }
  
}
