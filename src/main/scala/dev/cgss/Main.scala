package dev.cgss

import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.util.Timeout
import dev.cgss.core.OrderParserSystem

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.util.Failure

object Main extends App {

  implicit val system = ActorSystem(OrderParserSystem(), "DottChallengeScala")
  implicit val orderParserSystem: ActorRef[OrderParserSystem.Request] = system
  implicit val ec = system.executionContext

  implicit val timeout: Timeout = 1.minute
  val result: Future[OrderParserSystem.Response] = orderParserSystem.ask(ref => OrderParserSystem.ParseArgsRequest(args, ref))

  result.onComplete {
    case Failure(exception) => println(s"Failed ${exception.getMessage}")
    case util.Success(OrderParserSystem.WrongArgs(_)) => println("Usage: <begin date yyyy-MM-dd hh:mm:ss> <end date yyyy-MM-dd hh:mm:ss> <optional intervals>")
    case util.Success(OrderParserSystem.NumberOfOrdersByTimeRange(value)) =>
      value.foreach(result => println(s"${result._1.source} months: ${result._2}"))
  }

}
