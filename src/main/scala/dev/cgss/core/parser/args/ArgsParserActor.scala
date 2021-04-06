package dev.cgss.core.parser.args

import akka.actor.{Actor, ActorLogging}
import dev.cgss.core.parser.args.ArgsParserActor.{ArgsParseRequest, ArgsParserFailure, ParsedArgsResponse}
import dev.cgss.date.{DateParser, DateRange}

import scala.util.{Failure, Success, Try}

object ArgsParserActor {

  sealed trait Request

  sealed trait Response

  final case class ArgsParseRequest(args: Array[String]) extends Request

  final case class ParsedArgsResponse(parsedArgs: ParsedArgs) extends Response

  final case class ArgsParserFailure(ex: String) extends Response

}

class ArgsParserActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case ArgsParseRequest(args) => tryParseArgs(args) match {
      case Failure(_) => {
        log.debug("Incorrect args")
        sender() ! ArgsParserFailure("Usage: <begin date yyyy-MM-dd hh:mm:ss> <end date yyyy-MM-dd hh:mm:ss> <optional intervals>")
      }
      case Success(value) => {
        log.debug("Successfully parsed args")
        sender() ! ParsedArgsResponse(value)
      }
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
}

