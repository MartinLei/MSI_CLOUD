name := """animalProtect-app"""
version := "1.0-SNAPSHOT"

lazy val animalProtectApp = (project in file("."))
  .enablePlugins(PlayScala)


Universal / javaOptions ++= Seq(
  "-J-Xmx64m",
  // fix play bug with https in prod mode https://github.com/playframework/playframework/issues/11209
  "-J--add-exports=java.base/sun.security.x509=ALL-UNNAMED",
)

// play
libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.0" % Test

// logging
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.4.7"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5"

// google bucket
libraryDependencies += "com.google.cloud" % "google-cloud-storage" % "2.29.0"

// google firestore nosql
libraryDependencies += "com.google.firebase" % "firebase-admin" % "9.2.0"

// google secret manager
libraryDependencies += "com.google.cloud" % "google-cloud-secretmanager" % "2.32.0"
  