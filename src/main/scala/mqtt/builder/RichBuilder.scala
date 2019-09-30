package mqtt.builder

import mqtt.builder.BuildContext.Context
import mqtt.utils.Bit

import scala.reflect.ClassTag

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
  implicit class RichBuilderExtension[T: ClassTag](builder: Builder[T]) {
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
  
    /**
     * Or operator between builders. This operation returns a new builder typed with
     * the common ancestor of the two builders. The new builder will try to match
     * the build input with all the internal builder, the output is the result of all
     * matching builders concatenated.
     *
     * @param builder the other builder
     * @tparam C the common ancestor
     * @tparam B the other builder's build input type
     * @return A new builder representing the or of the twos
     */
    def ||[C >: T, B <: C : ClassTag](builder: Builder[B]): OrBuilder[C, T, B] = OrBuilder(this.builder, builder)
  }
  
  /**
   * Class that represent or operator between builders. This builder will
   * try to match the build input with all the internal builder, the output is the result
   * of all matching builders concatenated.
   *
   * @param builderA the first builder
   * @param builderB the second builder
   * @tparam C the common ancestor
   * @tparam A the first builder type
   * @tparam B the second builder type
   */
  case class OrBuilder[C, A <: C : ClassTag, B <: C : ClassTag](builderA: Builder[A], builderB: Builder[B]) extends Builder[C] {
    override def build[R <: C](value: R)(implicit context: Context[R]): Seq[Bit] =
      buildOption(value) match {
        case Some(seq) => seq
      }
    
    /**
     * Try to build with the given input, if none of the sub-builders matches then Option.empty is returned
     *
     * @param value the input value
     * @tparam R the input value's type
     * @return the build result, or empty if none of the sub.builders matches
     */
    def buildOption[R <: C](value: R): Option[Seq[Bit]] = {
      (buildA(value), buildB(value)) match {
        case (Some(a), Some(b)) => Option(a ++ b)
        case (Some(a), None) => Option(a)
        case (None, Some(b)) => Option(b)
        case (None, None) => None
      }
    }
    
    private def buildA[R <: C](value: R): Option[Seq[Bit]] = builderA match {
      case a: OrBuilder[C, _, _] => a.buildOption(value)
      case _ => value match {
        case a: A => Option(builderA.build(a))
        case _ => Option.empty
      }
    }
    
    private def buildB[R <: C](value: R): Option[Seq[Bit]] = builderB match {
      case b: OrBuilder[C, _, _] => b.buildOption(value)
      case _ => value match {
        case b: B => Option(builderB.build(b))
        case _ => Option.empty
      }
    }
  }
}