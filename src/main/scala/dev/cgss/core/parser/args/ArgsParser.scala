package dev.cgss.core.parser.args

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.Behaviors
import dev.cgss.date.{DateParser, DateRange}

import scala.util.{Failure, Success, Try}

object ArgsParser {

  def apply(): Behaviors.Receive[Request] = {
    Behaviors.receiveMessage[Request] {
      case ParseArgsRequest(args, actorRef) =>

        tryParseArgs(args) match {
          case Failure(ex) => actorRef ! WrongArgsResponse(ex.getMessage)
          case Success(value) => actorRef ! ParsedArgsResponse(value)
        }

        Behaviors.stopped
    }
  }

  def tryParseArgs(args: Array[String]): Try[ParsedArgs] = Try {
    if (args == null || args.isEmpty || args.length < 2 || args.length > 3) throw new ParserException
    else {
      val dateRange = DateRange(
        DateParser.fromDate(args(0)), DateParser.fromDate(args(1))
      )

      if (args.length == 3) {

        val intervals =
          args(2)
            .drop(1)
            .dropRight(1)
            .split(", ")
            .map(str => str.drop(1).dropRight(1))
            .map(DateParser.fromDateRange)
            .toSeq

        ParsedArgs(dateRange, intervals)
      } else {
        ParsedArgs(dateRange)
      }
    }
  }

  sealed trait Request

  sealed trait Response

  final case class ParseArgsRequest(args: Array[String], actorRef: ActorRef[Response]) extends Request

  final case class ParsedArgsResponse(parseArgs: ParsedArgs) extends Response

  final case class WrongArgsResponse(ex: String) extends Response

}

