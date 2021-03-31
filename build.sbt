name := "DottChallengeScala"
version := "0.1"
scalaVersion := "2.13.5"

val scalaTestVer = "3.2.6"
val scalaParallelVer = "1.0.2"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % scalaTestVer,
  "org.scalatest" %% "scalatest" % scalaTestVer % Test,
  "org.scala-lang.modules" %% "scala-parallel-collections" % scalaParallelVer
)