package dev.cgss.parser

import dev.cgss.date.{DateParser, DateRange}

import scala.util.Try

object ArgsParser {

  def apply(args: Array[String]): Try[ParsedArgs] = Try {
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
