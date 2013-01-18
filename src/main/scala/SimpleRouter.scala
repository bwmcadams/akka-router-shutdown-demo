package net.evilmonkeylabs.demo

import akka.actor.{ActorSystem, Actor, Props}
import akka.event.Logging
import akka.routing.RoundRobinRouter


object SimpleRouterSetup extends App {
  val system = ActorSystem("SimpleSystem")
  val simpleRouted = system.actorOf(Props[SimpleActor].withRouter(
                        RoundRobinRouter(nrOfInstances = 10)
                     ), name = "simpleRoutedActor")

  for (n <- 1 until 10)  simpleRouted ! Message("Hello, Akka #%d!".format(n))
}
