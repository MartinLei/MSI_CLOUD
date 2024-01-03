val scala3Version = "3.3.1"
val AkkaVersion = "2.9.0"

val dependencies = Seq(
  "org.scalatest" %% "scalatest" % "3.2.17" % Test,
  "org.scalatestplus" %% "mockito-3-4" % "3.2.10.0" % Test, "org.scalatest" %% "scalatest" % "3.2.15" % Test,
  // logging
  "ch.qos.logback" % "logback-classic" % "1.4.7",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  // play
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.0" % Test,
  // google bucket
  "com.google.cloud" % "google-cloud-storage" % "2.29.0",
  // google firestore nosql
  "com.google.firebase" % "firebase-admin" % "9.2.0",
  // akka streams alpakka
  "com.lightbend.akka" %% "akka-stream-alpakka-file" % "7.0.1",
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  // alpaka kafka
  "com.typesafe.akka" %% "akka-stream-kafka" % "4.0.2",
  // json serializer
  "io.circe" %% "circe-core" % "0.15.0-M1",
  "io.circe" %% "circe-generic" % "0.15.0-M1",
  "io.circe" %% "circe-parser" % "0.15.0-M1",
  "com.dripower" %% "play-circe" % "3014.1",
  // opencv
  "org.bytedeco" % "javacv" % "1.5.9",
  "org.bytedeco" % "javacv-platform" % "1.5.9",
  // wrapper for jodatime
  "com.github.nscala-time" %% "nscala-time" % "2.32.0"
)

lazy val animalProtectApp = project
  .in(file("."))
  .settings(
    name := "animalProtect-app",
    version := "2.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies ++= dependencies ,
    resolvers += "Akka library repository".at("https://repo.akka.io/maven"),
    javaOptions ++= Seq(
      "-J-Xmx64m",
      // fix play bug with https in prod mode https://github.com/playframework/playframework/issues/11209
      "-J--add-exports=java.base/sun.security.x509=ALL-UNNAMED",
    )
  )
  .enablePlugins(PlayScala)

