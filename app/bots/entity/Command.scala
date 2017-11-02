package bots.entity

import info.mukel.telegrambot4s.models.Message

/**
  * from the outside, that is what you send to the dispatcher,
  * and the dispatcher will forward to the correct subscription.
  * @param msg the message sent to the Bot
  * @param callbackData in case there was a callback, its data as string (e.g. if you provided a button)
  */
case class Command(msg: Message
                   , callbackData: Option[String] = None)

/**
  * if there is already an active conversation of a chatId, then this conversation must be restarted.
  */
case object RestartCommand

/**
  * if a command is for an SubscrAspect the dispatcher will sent an RunAspect to the active conversation.
  * @param command the command that was issued by the user.
  */
case class RunAspect(command: String, msg: Message)

object RunAspect {
  val logStateCommand = "/logstate"
}
