package net.evilmonkeylabs.demo

import akka.actor.{PoisonPill, ActorSystem, Actor, Props}
import akka.event.Logging


object SimplePoisoner extends App {
  val system = ActorSystem("SimpleSystem")
  val simpleActor = system.actorOf(Props[SimpleActor], name = "simple")

  simpleActor ! Message("Hello, Akka!")
  simpleActor ! PoisonPill
  simpleActor ! Message("Boy, that was some tasty arsenic!")
}
