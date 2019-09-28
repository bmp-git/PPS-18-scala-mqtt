package mqtt.utils

object RichOption {
  def on[A](cond: Boolean)(f: => A): Option[A] = if (cond) Some(f) else None
}
