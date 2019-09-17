package mqtt.broker

object StateImplicits {
  
  /**
   * Implicit to convert a function S => Either[E, (A, S)] in a StateTransitionWithError[S, A, E]
   */
  implicit def functionToStateTransitionWithError[S, A, E](f: S => Either[E, (A, S)]): StateTransitionWithError[S, A, E] = new StateTransitionWithError(f)
  
}
