package bots

import info.mukel.telegrambot4s.api.declarative.Commands
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}

object HelloBot extends TelegramBot with Polling with Commands {

  lazy val token: String = botToken

  onCommand('hello) { implicit msg => reply("Hello you!") }
}


object BotApp extends App {
  HelloBot.run()
}



