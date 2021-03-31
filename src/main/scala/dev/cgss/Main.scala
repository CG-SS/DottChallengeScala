package dev.cgss

import dev.cgss.date.DateRange
import dev.cgss.generator.OrderGenerator
import dev.cgss.order.Order
import dev.cgss.parser.{ArgsParser, ParsedArgs}

import scala.collection.parallel.CollectionConverters.ImmutableIterableIsParallelizable
import scala.collection.parallel.ParIterable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object Main extends App {

  ArgsParser(args) match {
    case Failure(_) => println("Usage: <begin date yyyy-MM-dd hh:mm:ss> <end date yyyy-MM-dd hh:mm:ss> <optional intervals>")
    case Success(parsedArgs: ParsedArgs) => start(parsedArgs)
  }

  def start(parsedArgs: ParsedArgs, orders: Seq[Order] = OrderGenerator()): Unit = {
    val validOrders = orders.par
      .filter(order => parsedArgs.orderDateRange contains order.creationDate)

    val parsedIntervals = parsedArgs
      .intervals
      .map(dateRange => processOrder(dateRange, validOrders))
    Future
      .sequence(parsedIntervals)
      .onComplete {
        case Failure(ex) => println(ex)
        case Success(seq) => seq.foreach(result => println(s"${result._1.source} months: ${result._2}"))
      }
  }

  def processOrder(dateRange: DateRange, validOrders: ParIterable[Order]): Future[(DateRange, Int)] = Future {
    val orderCount = validOrders.count(order => isProductInDateRange(order, dateRange))

    (dateRange, orderCount)
  }

  private def isProductInDateRange(order: Order, dateRange: DateRange): Boolean = {
    order
      .itemList
      .exists(item => dateRange contains item.product.creationDate)
  }

}
