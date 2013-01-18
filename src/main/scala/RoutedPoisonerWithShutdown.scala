package net.evilmonkeylabs.demo

import akka.actor._
import akka.routing.FromConfig

import akka.routing.Broadcast
import akka.event.Logging

class SystemKillingRouterOverwatch extends Actor {
  val log = Logging(context.system, this)

  // Setup watching of our other two actors
  override def preStart() {
    context.watch(RoutedPoisonerWithShutdown.router)
    context.watch(RoutedPoisonerWithShutdown.simpleActor)
  }

  def receive = {
    case Terminated(corpse) =>
      if (corpse == RoutedPoisonerWithShutdown.router) {
        log.warning("Received termination notification for '" + corpse + "'," +
                    "is in our watch list. Terminating ActorSystem.")
        RoutedPoisonerWithShutdown.system.shutdown()
      } else {
        log.info("Received termination notification for '" + corpse + "'," +
                 "which is not in our deathwatch list.".format(corpse))
      }
  }

}

object RoutedPoisonerWithShutdown extends App {
  val system = ActorSystem("SimpleSystem")
  val router = system.actorOf(Props[SimpleActor].withRouter(FromConfig()),
                              name = "simpleRoutedActor")
  val simpleActor = system.actorOf(Props[SimpleActor], name = "simpleActor")
  // Start him after the others so their refs are available and he can grab 'em (lazy code)
  val overwatch = system.actorOf(Props[SystemKillingRouterOverwatch])

  router ! Broadcast(Message("I will not buy this record, it is scratched!"))

  simpleActor ! Message("If there's any more stock film of women applauding, I'll clear the court.")

  simpleActor ! PoisonPill

  for (n <- 1 until 10)  router ! Message("Hello, Akka #%d!".format(n))
  router ! Broadcast(PoisonPill)
  router ! Message("Hello? You're looking a little green around the gills...") // never gets read


}
