package bots.examples.conversations

import javax.inject.{Inject, Named, Singleton}

import akka.actor.{ActorRef, ActorSystem, Props}
import pme.bots.control.ChatConversation
import pme.bots.entity.SubscrType.SubscrConversation
import pme.bots.entity.{Command, FSMState, Subscription}
import info.mukel.telegrambot4s.api.Extractors
import info.mukel.telegrambot4s.methods.EditMessageReplyMarkup
import info.mukel.telegrambot4s.models.{ChatId, InlineKeyboardButton, InlineKeyboardMarkup}

// @formatter:off
/**
  * counts the number user pushes the button and the number of requests
  *
  *     [Idle]
  *       v
  *   [Counting] <--------
  *       v              |
  *   markupCounter ------
  *
  */
// @formatter:on
class CounterConversation
  extends ChatConversation {

  private val TAG = callback
  private var requestCount = 0

  when(Idle) {
    case Event(Command(msg, _), _) =>
      sendMessage(msg, "Press to increment!"
        , replyMarkup = Some(markupCounter(0)))
      // tell where to go next
      goto(Counting)
    case other => notExpectedData(other)
  }

  when(Counting) {
    case Event(Command(msg, callbackData: Option[String]), _) =>
      info("Counting received command!")
      for {
        data <- callbackData
        Extractors.Int(n) = data
      } /* do */ {
        request(
          EditMessageReplyMarkup(
            Some(ChatId(msg.source)), // msg.chat.id
            Some(msg.messageId),
            replyMarkup = Some(markupCounter(n + 1))))
      }
      // this is a simple conversation that stays always in the same state.
      stay()
  }

  private  def markupCounter(n: Int): InlineKeyboardMarkup = {
    requestCount += 1
    InlineKeyboardMarkup.singleButton(
      InlineKeyboardButton.callbackData(
        s"Press me!!!\n$n - $requestCount",
        tag(n.toString)))
  }

  private def tag: String => String = prefixTag(TAG)

  // state to indicate that the count button is already shown to the User
  case object Counting extends FSMState
}

object CounterConversation {
  val command = "/counter"

  def props: Props = Props(new CounterConversation())
}

@Singleton
class CounterServiceSubscription @Inject()(@Named("commandDispatcher")
                                              val commandDispatcher: ActorRef
                                              , val system: ActorSystem) {
import CounterConversation._

  commandDispatcher ! Subscription(command, SubscrConversation
    , Some(_ => system.actorOf(props)))

}
