package net.evilmonkeylabs.demo

import akka.actor.{ActorSystem, Actor, Props}
import akka.event.Logging

case class Message(msg: String)

class SimpleActor extends Actor {
  val log = Logging(context.system, this)

  def receive = {
    case Message(msg) => log.info("Got a valid message: %s".format(msg))
    case default => log.error("Got a message I don't understand.")
  }
}

object SimpleMain extends App {
  val system = ActorSystem("SimpleSystem")
  val simpleActor = system.actorOf(Props[SimpleActor], name = "simple")

  simpleActor ! Message("Hello, Akka!") // toss a message into our actor with the "!" send op
}
