# play-scala-telegrambot4s

This page was originally created for a [JUGs workshop](https://www.jug.ch/html/events/2017/chatbot_programmieren.html).

The workshop is about the first steps on implementing a chat bot with:

* [Telegram](https://telegram.org) a Messaging app.
* [telegrambot4s](https://github.com/mukel/telegrambot4s) a Scala API for Telegram.

The following chapters document the workshop step by step.

**A basic understanding of SBT and Scala will help;).**

# Setup the Bot
* Register a user on [Telegram](https://telegram.org) if you don't have one.
* Search for the BotFather in the Telegram App (he helps you to setup and manage your bots).
* Ask the BotFather for a new bot: `/newbot`
* He will guide you through the simple process (nice example of a chat bot;).
* The generated token is all what you need!

# Setup the Project
* You need a SBT installation
* Clone this repo: `git clone https://github.com/pme123/play-scala-telegrambot4s.git`

The master branch contains only:

* [Giter8 template](https://github.com/playframework/play-scala-seed.g8)  for generating a Play project seed in Scala.
* the dependency: `libraryDependencies += "info.mukel" %% "telegrambot4s" % "3.0.14"`

# JSON-Client
(Here the [Solution Branch](https://github.com/pme123/play-scala-telegrambot4s/tree/add-hello-bot) if you have problems)

Let's use the provided [JSON-API from Telegram](https://core.telegram.org/api) directly. We only wan't to show the info about our bot on the index page.

First we want to **provide the token** in a safe way to avoid leaking it.

* Create a package object in the `bots` package and add:
```
lazy val botToken: String = scala.util.Properties
  .envOrNone("BOT_TOKEN")
  .getOrElse(Source.fromResource("bot.token").getLines().mkString)
```
* You can now add a `bot.token` with your token and put it in your `conf` folder.
* (or if security is of no concern, you can add it directly: `lazy val botToken = "[BOT_TOKEN]"`

Implement the **web-service client**:

* Add a dependency for the web-service client: `libraryDependencies += ws`
* Inject the web-socket client and the execution context (async processing) in the `HomeController`:
```
class HomeController @Inject()(cc: ControllerComponents
                               , ws: WSClient)
                              (implicit ec: ExecutionContext)
```
* The URL for the API is needed: `lazy val url = s"https://api.telegram.org/bot$botToken/getMe"`
* Adjust the `HomeController.index()` function:
```
  def index() = Action.async { implicit request: Request[AnyContent] =>
    ws.url(url)
      .get() // as this returns a Future the function is now async
      .map(_.json) // the the body as json
      .map(_.toString())
      .map(str => Ok(views.html.index(str))) // forward the result to the index page
  }
```
* Adjust the index.scala.html with the added parameter and render it:
```
@(myBot: String)

@main("Telegram Bots rock!") {
  <h1>Welcome to our Bot!</h1>
    <p>@myBot</p>
}
```
* Open the sbt console in the project base: play-scala-telegrambot4s>`sbt`
* Run the application [play-scala-telegrambot4s] $ `run`
* In a browser go to: [localhost:9000](http://localhost:9000)
* As result you should see a JSON string, like: `{"ok":true,"result":{"id":301276637,"is_bot":true,"first_name":"MeetupDemoBot","username":"MeetupDemoBot"}}`

That's it - let's move now to the [Scala API](https://github.com/mukel/telegrambot4s). For the next Bot examples we don't need the Play server - so you can shut it down.

# Webhooks vs Polling
Before we start let's explain shortly the difference between Webhooks and Polling. This is the quote from the [Scala API](https://github.com/mukel/telegrambot4s):
> Both methods are fully supported. Polling is the easiest method; it can be used locally without any additional requirements. It has been radically improved, doesn't flood the server (like other libraries do) and it's pretty fast.

> Using webhooks requires a server (it won't work on your laptop). For a comprehensive reference check Marvin's Patent Pending Guide to All Things Webhook.

So for this workshop, or examples in general **Polling** is the way to go.
# Hello User Bot
Let's greet the Bot and it should return that with a personalized greeting.

* Create a Scala object in the bots package: `HelloBot`
```
object HelloBot
  extends TelegramBot // the general bot behavior
    with Polling // we use Polling
    with Commands { // and we want to listen to Commands

  lazy val token: String = botToken // the token is required by the Bot behavior

  onCommand('hello) { implicit msg => // listen for the command hello and
    reply(s"Hello ${msg.from.map(_.firstName).getOrElse("")}!") // and reply with the personalized greeting
  }
}
```
* As we don't no infrastructure all we need is to run our Bot:
```
object BotApp extends App {
  HelloBot.run()
}
```
* Next we need our friend `BotFather` to create our command: `/setcommands`
* The easiest way is to manage your commands in file, from where you can take them (you always overwrite them for a bot). e.g. `botcommands.txt`:
```
hello - Simple Hello World.
```
* Like before the `BotFather` will help with this.
* Now lets say hello to your Bot (the command should be available, when hitting `/` in the text-field).

# Counter Bot
