package net.evilmonkeylabs.demo

import akka.actor._
import akka.routing.FromConfig

import akka.routing.Broadcast
import akka.event.Logging

class SystemKillingRouterOverwatch extends Actor {
  val log = Logging(context.system, this)
  /**
   * Terminated messages are generated for *all* actors, and we'll
   * need to make a list of ones we care about observing
   */
  val toWatch = scala.collection.mutable.Set.empty[ActorRef]

  def receive = {
    case ref: ActorRef =>
      toWatch += ref
      log.info("Now watching for termination events on Actor '%s'".format(ref))
      context.watch(ref) // Tell akka it should notify people about `ref` terminations
      sender ! true
    case Terminated(corpse) =>
      if (toWatch contains corpse) {
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
  val overwatch = system.actorOf(Props[SystemKillingRouterOverwatch])
  val router = system.actorOf(Props[SimpleActor].withRouter(FromConfig()),
                              name = "simpleRoutedActor")

  // Send a copy of the ActorRef to our overwatch actor
  import akka.pattern.ask
  import akka.util.duration._
  import akka.util.Timeout

  implicit val timeout = Timeout(5 seconds) // needed for `?`

  overwatch ? router // await a reply to ensure setup before continuing

  router ! Broadcast(Message("I will not buy this record, it is scratched!"))

  for (n <- 1 until 10)  router ! Message("Hello, Akka #%d!".format(n))
  router ! Broadcast(PoisonPill)
  router ! Message("Hello? You're looking a little green around the gills...") // never gets read


}
