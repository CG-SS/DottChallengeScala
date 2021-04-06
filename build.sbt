name := "DottChallengeScala"
version := "0.1"
scalaVersion := "2.13.5"

val akkaVer = "2.6.13"
val logbackVer = "1.2.3"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVer,
  "com.typesafe.akka" %% "akka-testkit" % akkaVer % Test
)