package bots.entity

import akka.actor.ActorRef

/**
  * To decouple the conversations, services and aspects, we can subscribe them to the dispatcher.
  *
  * @param command           e.g. /showconfig
  * @param subscrType        see SubscrType
  * @param chatFeatureConstr a constructor function that creates an ActorRef from a chatId.
  *                          (if there must be an instance for each chat.)
  */
case class Subscription(command: String
                        , subscrType: SubscrType
                        , chatFeatureConstr: Option[Long => ActorRef] = None)


/**
  * a category of a subscription.
  */
sealed trait SubscrType {
  def label: String
}

object SubscrType {

  /**
    * includes more than only one question.
    */
  case object SubscrConversation extends SubscrType {
    val label = "Conversation"
  }

  /**
    * a stateless request.
    */
  case object SubscrService extends SubscrType {
    val label = "Service"
  }

  /**
    * If you want to add a functionality for all conversations, you can create an aspect.
    * Be aware that you need to handle it in the conversations.
    * Example: /logstate
    */
  case object SubscrAspect extends SubscrType {
    val label = "Aspect"
  }

}
