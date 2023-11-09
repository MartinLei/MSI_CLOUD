name := """cloud"""
organization := "de.htwg"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "3.3.1"

// play
libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "6.0.0-RC2" % Test

// postgresql connection db
libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "5.2.0-RC1",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.2.0-RC1",
  "org.postgresql" % "postgresql" % "42.6.0"
)

// google bucket
libraryDependencies += "com.google.cloud" % "google-cloud-storage" % "2.29.0"

// google firestore nosql
libraryDependencies += "com.google.firebase" % "firebase-admin" % "9.2.0"
