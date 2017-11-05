package bots

import info.mukel.telegrambot4s.api.declarative.Commands
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}

object HelloBot
  extends TelegramBot // the general bot behavior
    with Polling // we use Polling
    with Commands { // and we want to listen to Commands

  lazy val token: String = botToken // the token is required by the Bot behavior

  onCommand('hello) { implicit msg => // listen for the command hello and
    reply(s"Hello ${msg.from.map(_.firstName).getOrElse("")}!") // and reply with the personalized greeting
  }
}


object BotApp extends App {
  HelloBot.run()
}



