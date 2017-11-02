import bots.boundary.BotRunner
import bots.control.{CommandDispatcher, LogStateSubscription}
import bots.examples.conversations.CounterServiceSubscription
import bots.examples.services.HelloServiceSubscription
import com.google.inject.AbstractModule
import play.api.Logger
import play.api.libs.concurrent.AkkaGuiceSupport

/**
  * This class is a Guice module that tells Guice how to bind several
  * different types. This Guice module is created when the Play
  * application starts.
  *
  */
class Module
  extends AbstractModule
    with AkkaGuiceSupport {
  private val log = Logger(getClass)

  override def configure() {
    log.info("config modules")
    bindActor[CommandDispatcher]("commandDispatcher")
    bind(classOf[BotRunner]).asEagerSingleton()
    bind(classOf[HelloServiceSubscription]).asEagerSingleton()
    bind(classOf[CounterServiceSubscription]).asEagerSingleton()
    bind(classOf[LogStateSubscription]).asEagerSingleton()

  }

}
