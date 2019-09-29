package mqtt.broker.handlers

import mqtt.broker.state.{State, StateTransitionWithError, Violation}

/**
 * A mixin that defines an implicit function that recovers a StateTransitionWithError[State, _, Violation]
 * converting it to a function State => State, handling the violation if present.
 */
trait AutoViolationHandler {
  this: PacketHandler[_] =>
  
  /**
   * Converts a StateTransitionWithError[State, _, Violation] into a function State => State, handling the violation if present.
   *
   * @param transition the StateTransitionWithError[State, _, Violation]
   * @return a function that maps the old server state in the new one.
   */
  implicit def handleViolation(transition: StateTransitionWithError[State, _, Violation]): State => State = state => {
    transition run state match {
      //TODO remove println, use a logger
      case Left(v) => println(v.toString); v.handle(channel)(state) //close connection in case of error
      case Right((_, s)) => s
    }
  }
}
