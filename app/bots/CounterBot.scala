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

  private val TAG = "COUNTER_TAG"
  private var requestCount = 0

  private def markupCounter(n: Int): InlineKeyboardMarkup = {
    requestCount += 1
    InlineKeyboardMarkup.singleButton( // set a layout for the Button
      InlineKeyboardButton.callbackData( // create the button into the layout
        s"Press me!!!\n$n - $requestCount", // text to show on the button (count of the times hitting the button and total request count)
        tag(n.toString))) // create a callback identifier
  }

  private def tag: String => String = prefixTag(TAG)

  onCommand("/counter") { implicit msg =>
    reply("Press to increment!", replyMarkup = Some(markupCounter(0)))
  }

  onCallbackWithTag(TAG) { implicit cbq => // listens on all callbacks that START with TAG
    // Notification only shown to the user who pressed the button.
    ackCallback(Some(cbq.from.firstName + " pressed the button!"))
    // Or just ackCallback() - this is needed by Telegram!

    for {
      data <- cbq.data //the data is the callback identifier without the TAG (the count in our case)
      Extractors.Int(n) = data // extract the optional String to an Int
      msg <- cbq.message
    } /* do */ {
      request(
        EditMessageReplyMarkup( // to update the existing button - (not creating a new button)
          Some(ChatId(msg.source)), // msg.chat.id
          Some(msg.messageId),
          replyMarkup = Some(markupCounter(n + 1))))
    }
  }
}

object CounterBotApp extends App {
  CounterBot.run()
}



