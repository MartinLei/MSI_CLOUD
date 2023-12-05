name := "animalProtect-root"
Global / scalaVersion := "3.3.1"

lazy val root = project.in(file("."))
  //.settings(commonSettings)

lazy val animalProtectApp = project
  .enablePlugins(PlayScala)
  .settings(commonSettings)


lazy val commonSettings = Seq(
  organization := "de.htwg.msi.cda.animalprotect",
  scalaVersion := "3.3.1",
  scalacOptions ++= Seq("-deprecation", "-feature"),
  libraryDependencies ++= commonDependencies
)

lazy val commonDependencies = Seq(
  // play
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.0" % Test,

  // logging
  "ch.qos.logback" % "logback-classic" % "1.4.7",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5"
)