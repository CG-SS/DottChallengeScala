package dev.cgss


import akka.actor.ActorSystem

object Main extends App {

  val system = ActorSystem("DottChallenge")
  val mainActor = system.actorOf(MainActor.props(args), "MainActor")

  mainActor ! MainActor.Start

}
