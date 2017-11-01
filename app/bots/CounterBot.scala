package bots

import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.api.{Extractors, Polling, TelegramBot}
import info.mukel.telegrambot4s.methods.EditMessageReplyMarkup
import info.mukel.telegrambot4s.models.{ChatId, InlineKeyboardButton, InlineKeyboardMarkup}

object CounterBot
  extends TelegramBot
    with Polling
    with Commands
    with Callbacks {

  lazy val token: String = botToken

  val TAG = "COUNTER_TAG"
  var requestCount = 0

  def markupCounter(n: Int): InlineKeyboardMarkup = {
    requestCount += 1
    InlineKeyboardMarkup.singleButton(
      InlineKeyboardButton.callbackData(
        s"Press me!!!\n$n - $requestCount",
        tag(n.toString)))
  }

  def tag: String => String = prefixTag(TAG)

  onCommand("/counter") { implicit msg =>
    reply("Press to increment!", replyMarkup = Some(markupCounter(0)))
  }

  onCallbackWithTag(TAG) { implicit cbq =>
    // Notification only shown to the user who pressed the button.
    ackCallback(Some(cbq.from.firstName + " pressed the button!"))
    // Or just ackCallback()

    for {
      data <- cbq.data
      Extractors.Int(n) = data
      msg <- cbq.message
    } /* do */ {
      request(
        EditMessageReplyMarkup(
          Some(ChatId(msg.source)), // msg.chat.id
          Some(msg.messageId),
          replyMarkup = Some(markupCounter(n + 1))))
    }
  }
}

object CounterBotApp extends App {
  CounterBot.run()
}



