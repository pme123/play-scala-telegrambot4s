package bots.control

import akka.actor.{Actor, ActorRef, PoisonPill}
import bots.botToken
import bots.entity.SubscrType.{SubscrAspect, SubscrConversation, SubscrService}
import bots.entity._
import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.methods.SendMessage
import info.mukel.telegrambot4s.models.Message

import scala.collection.mutable

/**
  * Created by pascal.mengelt on 08.01.2017.
  */
class CommandDispatcher
  extends Actor
    with TelegramBot
    with Polling
    with Commands
    with Callbacks
    with Logger {

  def token: String = botToken

  private val subscriptions: mutable.Map[String, Subscription] = mutable.Map()
  private val conversations: mutable.Map[Long, (Subscription, ActorRef)] = mutable.Map()

  def receive: PartialFunction[Any, Unit] = {
    case subscr: Subscription =>
      info(s"received Subscription: for Command: ${subscr.command} - ${subscr.subscrType.label}")
      subscriptions.put(subscr.command, subscr)
    case Command(msg: Message, callbackData) =>
      msg.text.flatMap(subscriptions.get) match {
        case Some(subscription) => // check if it is a control
          subscription.subscrType match {
            case SubscrAspect =>
              conversations.get(msg.chat.id) match {
                case Some((_, actorRef)) => actorRef ! RunAspect(subscription.command, msg)
                case None => request(SendMessage(msg.source, initChat))
              }
            case SubscrService =>
              subscription.chatFeatureConstr.get(msg.chat.id) ! Command(msg)
            case SubscrConversation =>
              info("SubscrConversation: " + subscription)
              getOrCreate(msg, subscription) ! Command(msg)
          }

        case None => // else get the active control for that Chat
          conversations.get(msg.chat.id) match {
            case Some((_, actorRef)) => actorRef ! Command(msg, callbackData)
            case None => request(SendMessage(msg.source, initChat))
          }
      }
    case _ => info("received unknown message")
  }

  private def getOrCreate(msg: Message, subscription: Subscription): ActorRef =
    conversations.get(msg.chat.id)
      .flatMap { case (subscr, actorRef) =>
        if (subscr == subscription) {
          actorRef ! RestartCommand
          Some(actorRef)
        } else {
          actorRef ! PoisonPill
          None
        }
      }.getOrElse(newConversation(msg, subscription))


  private def newConversation(msg: Message, subscription: Subscription): ActorRef = {
    info(s"newConversation: ${msg.chat.id} for $subscription")
    val newActorRef = subscription.chatFeatureConstr.get(msg.chat.id)
    conversations.update(msg.chat.id, (subscription, newActorRef))
    newActorRef
  }

  private def initChat = "Sorry, there is no active control. First choose a control, here are all of them:\n" +
    (subscriptions.values
      .filter(_.subscrType == SubscrConversation)
      .map(_.command)
      .toList.sorted mkString "\n")

}
