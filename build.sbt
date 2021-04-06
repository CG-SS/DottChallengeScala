name := "DottChallengeScala"
version := "0.1"
scalaVersion := "2.13.5"

val akkaVer = "2.6.13"
val scalaTestVer = "3.2.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVer,
  "com.typesafe.akka" %% "akka-testkit" % akkaVer % Test,
  "org.scalatest" %% "scalatest" % scalaTestVer,
  "org.scalatest" %% "scalatest" % scalaTestVer % Test
)