package dev.cgss


import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object Main extends App {

  val debugConfig = ConfigFactory.load().getConfig("release")
  val system = ActorSystem("DottChallenge", debugConfig)
  val mainActor = system.actorOf(MainActor.props(args), "MainActor")

  mainActor ! MainActor.Start

}
