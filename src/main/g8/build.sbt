
lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.11.8",
      libraryDependencies ++= List(
        "com.typesafe.akka" %% "akka-http" % "10.0.9",
        "org.scalatra.scalate" %% "scalate-core" % "1.8.0",
        "org.slf4j" % "slf4j-nop" % "1.7.25"
      )
    )),
    name := "oauth2-tutorial"
  )
