package bots.boundary

import javax.inject.{Inject, Named, Singleton}

import akka.actor.ActorRef
import bots._
import bots.entity.{Command, Logger}
import info.mukel.telegrambot4s.api.declarative.Callbacks
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.methods.SendMessage
import info.mukel.telegrambot4s.models._
import play.api.Configuration
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

/**
  * Created by pascal.mengelt on 27.12.2016.
  */
class TelegramBoundary @Inject()(@Named("commandDispatcher") val commandDispatcher: ActorRef
                                 , lifecycle: ApplicationLifecycle)
                                (implicit config: Configuration)
  extends TelegramBot
    with Polling
    with Callbacks
    with Logger {

  def token: String = botToken
  override def pollingInterval: Int = 2

  override def receiveMessage(msg: Message): Unit = {
    checkUser(msg).foreach(_ =>
      commandDispatcher ! Command(msg)
    )
  }

  onCallbackWithTag(callback) { implicit cbq =>
    // Notification otherwise Telegram gets nervous.
    info(s"callbackdata: ${cbq.data}")
    ackCallback()
    for {
      data <- cbq.data.map(_.stripPrefix(callback))
      msg <- cbq.message
    } {
      //noinspection UnitInMap
      commandDispatcher ! Command(msg, Some(data))
    }
  }

  private lazy val acceptUsers: List[Int] = config.get[String]("bots.accept.users").split(",").toList.filter(_.trim.nonEmpty).map(_.toInt)

  private def checkUser(msg: Message): Option[User] = {
    val user = msg.from.get
    val acceptsUser = acceptUsers.isEmpty || acceptUsers.contains(user.id)
    if (acceptsUser)
      Some(user)
    else {
      warn(s"A User tried to access the SFnImportAdapter without rights: $user")
      request(SendMessage(msg.source, "Sorry you don't have the rights to talk to me.\n" +
        "Please contact your admin if you need access."))
      None
    }
  }

  // Shut-down hook
  lifecycle.addStopHook { () =>
    shutdown()
  }
}

@Singleton
class BotRunner @Inject()(telegramBot: TelegramBoundary)
  extends Logger {
  info("initialized TelegramBoundary")
  telegramBot.run()
}
