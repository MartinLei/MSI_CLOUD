name := "cloudBox-root"

Global / organization := "de.htwg.msi.cda.cloudbox"

Global / scalaVersion := "3.3.1"

lazy val root = project.in(file("."))

lazy val cloudBoxApp = project
  .enablePlugins(PlayScala)

lazy val imageRecognitionApp = project
  .enablePlugins(PlayScala)
