package dev.cgss.parser

import org.scalatest.funsuite.AnyFunSuite

class ArgsParserTest extends AnyFunSuite {

  test("ArgsParser.apply wrong args") {
    assert(
      ArgsParser(Array("2018-01-01 00:00:00", "2019-01-01")).isFailure
    )
  }

  test("ArgsParser.apply valid args") {
    assert(
      ArgsParser(Array("2018-01-01 00:00:00", "2019-01-01 00:00:00")).isSuccess
    )
  }

  test("ArgsParser.apply valid args with intervals") {
    assert(
      ArgsParser(Array("2018-01-01 00:00:00", "2019-01-01 00:00:00", """("1-2", "1-12", ">16")""")).isSuccess
    )
  }
}
