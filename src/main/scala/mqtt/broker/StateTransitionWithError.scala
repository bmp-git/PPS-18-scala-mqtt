package mqtt.broker

import mqtt.broker.StateTransitionWithError.unit

/**
 * Represents a state transition.
 * The transition can be successful returning a Right with an output and a new state.
 * The transition can fail returning a Left with the error.
 *
 * @param run runs the transition.
 * @tparam S the state type.
 * @tparam A the output type.
 * @tparam E the error type.
 */
class StateTransitionWithError[S, A, E](val run: S => Either[E, (A, S)]) {
  def map[B](f: A => B): StateTransitionWithError[S, B, E] =
    flatMap(a => unit(f(a)))
  
  def flatMap[B](f: A => StateTransitionWithError[S, B, E]): StateTransitionWithError[S, B, E] = new StateTransitionWithError(s => {
    val either = run(s)
    either match {
      case Left(value) => Left(value)
      case Right((a, s1)) => f(a).run(s1)
    }
  })
}

object StateTransitionWithError {
  def unit[S, A, E](a: A): StateTransitionWithError[S, A, E] =
    new StateTransitionWithError(s => Right((a, s)))
}