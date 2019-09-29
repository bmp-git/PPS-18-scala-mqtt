package mqtt.builder

import mqtt.builder.BuildContext.Context
import mqtt.builder.CommonBuilders._
import mqtt.builder.RichBuilder._
import mqtt.model.QoS
import mqtt.utils.Bit

import scala.concurrent.duration.Duration

/**
 * Provides some implicits in order to facilitate the definition of MQTT 3.1.1 packet structures.
 * Example: a builder for a certain string field can be defined as:
 * "val myStringFieldBuilder: Builder[MyPacket] = (p: MyPacket) => p.myStringField";
 * it will produces an encoded string as specified at 1.5.3.
 */
object BuilderImplicits {
  implicit def fromBooleanExtractorToBuilder[T](ex: T => Boolean): Builder[T] = rawBit from ex
  
  implicit def fromBitSeqExtractorToBuilder[T](ex: T => Seq[Bit]): Builder[T] = rawBits from ex
  
  implicit def fromStringExtractorToBuilder[T](ex: T => String): Builder[T] = stringBuilder from ex
  
  implicit def fromOptionStringExtractorToBuilder[T](ex: T => Option[String]): Builder[T] = (t: T) => ex(t).fold(empty)(stringBuilder of _)
  
  implicit def fromQoSExtractorToBuilder[T](ex: T => QoS): Builder[T] = qosBuilder from ex
  
  implicit def fromDurationExtractorToBuilder[T](ex: T => Duration): Builder[T] = keepAliveBuilder from ex
  
  implicit def dynamicBuilder[T](selector: T => Builder[T]): Builder[T] = new Builder[T] {
    override def build[R <: T](value: R)(implicit context: Context[R]): Seq[Bit] = selector(value).build(value)
  }
}
