name := """cloud"""
organization := "de.htwg"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "3.3.1"

// play dependencies
libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "6.0.0-RC2" % Test

// db connection dependencies
libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "5.2.0-RC1",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.2.0-RC1",
  "org.postgresql" % "postgresql" % "42.6.0"
)