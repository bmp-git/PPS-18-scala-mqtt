package mqtt.broker

object StateImplicits {
  
  /**
   * Implicit to convert a function S => Either[E, (A, S)] in a StateTransitionWithError[S, A, E]
   */
  implicit class StateTransitionWithError_Implicit[S, A, E](override val run: S => Either[E, (A, S)]) extends StateTransitionWithError[S, A, E](run)
  
}
