package bots.examples.services

import javax.inject.{Inject, Named, Singleton}

import akka.actor.{ActorRef, ActorSystem, Props}
import bots.control.ChatService
import bots.entity.SubscrType.SubscrService
import bots.entity.{Command, Subscription}

/**
  * says hello to the user
  * (Hello World)
  * Example of a ChatService.
  * That is if there is no state at all (no conversation)
  */
case class HelloService()
  extends ChatService {

  def receive: Receive = {
    case Command(msg, _) =>
      sendMessage(msg, s"hello ${msg.from.map(u => u.firstName + " " +u.lastName.getOrElse("")).getOrElse("") }")
    case other => warn(s"Not expected message: $other")
  }
}

object HelloService {
  val command = "/hello"

  def props: Props = Props(HelloService())
}

@Singleton
class HelloServiceSubscription @Inject()(@Named("commandDispatcher")
                                              val commandDispatcher: ActorRef
                                              , val system: ActorSystem) {
import HelloService._

  commandDispatcher ! Subscription(command, SubscrService
    , Some(_ => system.actorOf(props)))

}
