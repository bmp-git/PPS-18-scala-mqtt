package mqtt.broker.handlers

import mqtt.broker.state.{State, StateTransitionWithError, Violation}

trait AutoViolationHandler {
  this: PacketHandler[_] =>
  implicit def handleViolation(transition: StateTransitionWithError[State, _, Violation]): State => State = state => {
    transition run state match {
      //TODO remove println, use a logger
      case Left(v) => println(v.toString); v.handle(channel)(state) //close connection in case of error
      case Right((_, s)) => s
    }
  }
}
