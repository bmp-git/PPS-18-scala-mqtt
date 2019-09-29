package mqtt.broker.state

object StateImplicits {
  
  /**
   * Implicit to convert a function S => Either[E, (A, S)] in a StateTransitionWithError[S, A, E].
   */
  implicit def functionToSTWE[S, A, E](f: S => Either[E, (A, S)]): StateTransitionWithError[S, A, E] = new StateTransitionWithError(f)
  
  /**
   * Implicit to convert a function S => S in a StateTransitionWithError[S, Unit, E].
   */
  implicit def stateToStateToSTWE[S, E](f: S => S): StateTransitionWithError[S, Unit, E] = {
    new StateTransitionWithError(s => Right(((), f(s))))
  }
  
  /**
   * Implicit to convert a function S => Either[E, S] in a StateTransitionWithError[S, Unit, E].
   */
  implicit def stateToEitherToSTWE[S, E](f: S => Either[E, S]): StateTransitionWithError[S, Unit, E] = {
    new StateTransitionWithError(s => f(s).map(((), _)))
  }
  
  /**
   * Implicit to convert a function S => (A, S) in a StateTransitionWithError[S, A, E].
   */
  implicit def stateToStateAndOutToSTWE[S, A, E](f: S => (A, S)): StateTransitionWithError[S, A, E] = {
    new StateTransitionWithError(s => Right(f(s)))
  }
  
  /**
   * Implicit to convert an Either[E, (Unit, S)] in an Either[E, S].
   */
  implicit def eitherRemoveUnit[S, E](either: Either[E, (Unit, S)]): Either[E, S] = either.map { case (_, s) => s }
}
