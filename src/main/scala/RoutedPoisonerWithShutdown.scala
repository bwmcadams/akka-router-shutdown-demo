package net.evilmonkeylabs.demo

import akka.actor._
import akka.routing.{RoundRobinRouter, FromConfig, Broadcast}

import akka.event.Logging

class SystemKillingRouterOverwatch extends Actor {
  val log = Logging(context.system, this)

  val simpleRouter = context.actorOf(Props[SimpleActor].withRouter(FromConfig()),
                                     name = "simpleRoutedActor")

  val simpleActor = context.actorOf(Props[SimpleActor], name = "simpleActor")

  // Setup our other two actors, so we supervise
  context.watch(simpleRouter)
  context.watch(simpleActor)

  def receive = {
    case Terminated(corpse) =>
      if (corpse == simpleRouter) {
        log.warning("Received termination notification for '" + corpse + "'," +
          "is in our watch list. Terminating ActorSystem.")
        RoutedPoisonerWithShutdown.system.shutdown()
      } else {
        log.info("Received termination notification for '" + corpse + "'," +
          "which is not in our deathwatch list.".format(corpse))
      }
  }

  simpleRouter ! Broadcast(Message("I will not buy this record, it is scratched!"))

  simpleActor ! Message("If there's any more stock film of women applauding, I'll clear the court.")

  simpleActor ! PoisonPill

  for (n <- 1 until 10) simpleRouter ! Message("Hello, Akka #%d!".format(n))
  simpleRouter ! Broadcast(PoisonPill)
  simpleRouter ! Message("Hello? You're looking a little green around the gills...") // never gets read


}

object RoutedPoisonerWithShutdown extends App {
  val system = ActorSystem("SimpleSystem")
  val overwatch = system.actorOf(Props[SystemKillingRouterOverwatch], name="overwatch")

}
