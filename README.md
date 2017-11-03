# play-scala-telegrambot4s

This page was originally created for a [JUGs workshop](https://www.jug.ch/html/events/2017/chatbot_programmieren.html).

The workshop is about the first steps on implementing a chat bot with:
* [Telegram](https://telegram.org) a Messaging app.
* [telegrambot4s](https://github.com/mukel/telegrambot4s) a Scala API for Telegram.

The following chapters document the workshop step by step.

# Setup
* You need a SBT installation
* Clone this repo: `git clone https://github.com/pme123/play-scala-telegrambot4s.git`

The master branch contains only:
* [Giter8 template](https://github.com/playframework/play-scala-seed.g8)  for generating a Play project seed in Scala.
* the dependency: `libraryDependencies += "info.mukel" %% "telegrambot4s" % "3.0.14"`
)

# JSON-Client
First let's use the provided [JSON-API from Telegram](https://core.telegram.org/api) directly.
* Switch to the branch: `git check`
