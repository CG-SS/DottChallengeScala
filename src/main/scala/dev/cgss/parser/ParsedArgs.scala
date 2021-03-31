package dev.cgss.parser

import dev.cgss.date.{DateParser, DateRange}

final case class ParsedArgs(orderDateRange: DateRange, intervals: Seq[DateRange] = Seq("1-3", "4-6", "7-12", ">12").map(DateParser.fromDateRange))
