package net.evilmonkeylabs.demo

import akka.actor.{PoisonPill, ActorSystem, Actor, Props}
import akka.event.Logging
import akka.routing.{FromConfig, RoundRobinRouter}



object RoutedPoisoner extends App {
  val system = ActorSystem("SimpleSystem")
  val simpleRouter = system.actorOf(Props[SimpleActor].withRouter(FromConfig()),
                                    name = "simpleRoutedActor")

  import akka.routing.Broadcast
  simpleRouter ! Broadcast(Message("I will not buy this record, it is scratched!"))

  for (n <- 1 until 10)  simpleRouter ! Message("Hello, Akka #%d!".format(n))
  simpleRouter ! Broadcast(PoisonPill)
  simpleRouter ! Message("Hello? You're looking a little green around the gills...") // never gets read
}
