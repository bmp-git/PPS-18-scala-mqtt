package mqtt.broker

object StateImplicits {
  
  implicit class StateTransitionWithError_Implicit[S, A, E](override val run: S => Either[E, (A, S)]) extends StateTransitionWithError[S, A, E](run)
  
}
