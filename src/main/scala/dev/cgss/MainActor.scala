package dev.cgss

import akka.actor.{Actor, Props}
import akka.pattern.ask
import akka.util.Timeout
import dev.cgss.MainActor.Start
import dev.cgss.core.OrderParserSystem

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

object MainActor {

  def props(args: Array[String]): Props = Props(new MainActor(args))

  sealed trait Request

  case object Start extends Request
}

class MainActor(private val args: Array[String]) extends Actor {

  private implicit val defaultTimeout: Timeout = 1.minute

  override def receive: Receive = {
    case Start => {
      val orderParserSystem = context.actorOf(Props[OrderParserSystem], "OrderParserSystem")

      (orderParserSystem ? OrderParserSystem.ParseOrdersByDateRange(args))
        .mapTo[OrderParserSystem.Response].onComplete {
        case Failure(exception) => {
          println(s"Error: ${exception.getMessage}")
          context.stop(self)
        }
        case Success(value) => value match {
          case OrderParserSystem.FailureResponse(ex) => println(ex)
          case OrderParserSystem.ParsedOrdersByDateRange(result) => result.foreach(result => println(s"${result._1.source} months: ${result._2}"))
        }
      }
    }
  }
}
