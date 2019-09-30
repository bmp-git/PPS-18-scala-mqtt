package mqtt.builder
import mqtt.builder.BuildContext.Context
import mqtt.utils.Bit

trait IdentityBuilder[-P] extends Builder[P] {
  override def build[R <: P](value: R)(implicit context: Context[R]): Seq[Bit] = builder.build(value)(context)
  
  def builder: Builder[P]
}
