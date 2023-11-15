name := """image-recognition-app"""
version := "1.0-SNAPSHOT"

lazy val imageRecognitionApp = (project in file("."))
  .enablePlugins(PlayScala)

// play
libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.0" % Test

// logging
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.4.7"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5"
