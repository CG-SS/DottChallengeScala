package dev.cgss

import dev.cgss.date.{DateParser, DateRange}
import dev.cgss.order.{Item, Order, Product => OrderProduct}
import dev.cgss.parser.ParsedArgs
import org.scalatest.funsuite.AnyFunSuite

import java.time.LocalDateTime
import scala.collection.parallel.CollectionConverters.ImmutableIterableIsParallelizable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class MainTest extends AnyFunSuite {

  test("Main.processOrder 1 2 3 4 order") {
    val parsedArgs = ParsedArgs(
      DateRange(
        DateParser.fromDate("2018-01-01 00:00:00"),
        DateParser.fromDate("2019-01-01 00:00:00")
      )
    )

    val now = LocalDateTime.now()
    val twelveMonthsPlusItems = Seq(Item(0, 0, 0, OrderProduct("", "", 0, 0, now.minusMonths(13))))
    val sevenToTwelveMonthsItems = Seq(Item(0, 0, 0, OrderProduct("", "", 0, 0, now.minusMonths(8))))
    val fourToSixMonthsItems = Seq(Item(0, 0, 0, OrderProduct("", "", 0, 0, now.minusMonths(6))))
    val oneToThreeMonthsItems = Seq(Item(0, 0, 0, OrderProduct("", "", 0, 0, now.minusMonths(1))))


    val orders =
      Seq(
        Order("", "", "", 0, creationDate = DateParser.fromDate("2018-01-01 00:00:00"),
          itemList = twelveMonthsPlusItems ++ sevenToTwelveMonthsItems ++ fourToSixMonthsItems ++ oneToThreeMonthsItems
        ),
        Order("", "", "", 0, creationDate = DateParser.fromDate("2018-01-01 00:00:00"),
          itemList = twelveMonthsPlusItems ++ sevenToTwelveMonthsItems ++ fourToSixMonthsItems
        ),
        Order("", "", "", 0, creationDate = DateParser.fromDate("2018-01-01 00:00:00"),
          itemList = twelveMonthsPlusItems ++ sevenToTwelveMonthsItems
        ),
        Order("", "", "", 0, creationDate = DateParser.fromDate("2018-01-01 00:00:00"),
          itemList = twelveMonthsPlusItems
        )
      )

    val parsedIntervals = parsedArgs
      .intervals
      .map(dateRange => Main.processOrder(dateRange, orders.par))
    Future
      .sequence(parsedIntervals)
      .onComplete {
        case Failure(ex) => fail(ex.getMessage)
        case Success(value) =>
          if (value.length != 4) {
            fail(s"Expected size is 4, got ${value.length}")
          } else {
            if (value.head._2 == 1 && value(1)._2 == 2 && value(2)._2 == 3 && value(3)._2 == 4) {
              succeed
            } else {
              fail("Wrong order count for intervals")
            }
          }
      }
  }

}
