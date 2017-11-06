name := """play-scala-telegrambot4s"""
organization := "pme"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.3"

libraryDependencies += ws
libraryDependencies += guice
libraryDependencies += "com.github.pme123" % "play-akka-telegrambot4s" % "0.0.2"

libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test

resolvers += "jitpack" at "https://jitpack.io"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "pme.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "pme.binders._"
