name := """cloud"""
organization := "de.htwg"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "3.3.1"

// play
libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "6.0.0-RC2" % Test

// logging
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.10"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5"

// google bucket
libraryDependencies += "com.google.cloud" % "google-cloud-storage" % "2.29.0"

// google firestore nosql
libraryDependencies += "com.google.firebase" % "firebase-admin" % "9.2.0"


