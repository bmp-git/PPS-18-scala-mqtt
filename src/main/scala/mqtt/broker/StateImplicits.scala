package mqtt.broker

object StateImplicits {
  
  /**
   * Implicit to convert a function S => Either[E, (A, S)] in a StateTransitionWithError[S, A, E]
   */
  implicit def functionToSTWE[S, A, E](f: S => Either[E, (A, S)]): StateTransitionWithError[S, A, E] = new StateTransitionWithError(f)
  
  implicit def stateToStateToSTWE[S, E](f: S => S): StateTransitionWithError[S, Unit, E] = {
    new StateTransitionWithError(s => Right(((), f(s))))
  }
  
  implicit def stateToEitherToSTWE[S, E](f: S => Either[E, S]): StateTransitionWithError[S, Unit, E] = {
    new StateTransitionWithError(s => f(s).map(((), _)))
  }
  
  implicit def stateToStateAndOutToSTWE[S, A, E](f: S => (A, S)): StateTransitionWithError[S, A, E] = {
    new StateTransitionWithError(s => Right(f(s)))
  }
  
  implicit def eitherRemoveUnit[S, E](either: Either[E, (Unit, S)]): Either[E, S] = either.map { case (_, s) => s }
}
