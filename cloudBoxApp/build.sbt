name := """cloudBox-app"""
version := "1.0-SNAPSHOT"

lazy val cloudBoxApp = (project in file("."))
  .enablePlugins(PlayScala)


Universal / javaOptions ++= Seq(
  "-J-Xmx64m",
  // fix play bug with https in prod mode https://github.com/playframework/playframework/issues/11209
  "-J--add-exports=java.base/sun.security.x509=ALL-UNNAMED",
)


// google bucket
libraryDependencies += "com.google.cloud" % "google-cloud-storage" % "2.29.0"

// google firestore nosql
libraryDependencies += "com.google.firebase" % "firebase-admin" % "9.2.0"
