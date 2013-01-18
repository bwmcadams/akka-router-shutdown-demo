package net.evilmonkeylabs.demo
import akka.actor.{ActorSystem, Actor, Props}
import akka.event.Logging
import akka.routing.{FromConfig, RoundRobinRouter}


object SimpleFileConfiggedRouterSetup extends App {
  val system = ActorSystem("SimpleSystem")
  val simpleRouted = system.actorOf(Props[SimpleActor].withRouter(FromConfig()),
                                    name = "simpleRoutedActor")

  for (n <- 1 until 10)  simpleRouted ! Message("Hello, Akka #%d!".format(n))
}
