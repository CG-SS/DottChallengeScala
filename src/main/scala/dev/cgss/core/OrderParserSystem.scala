package dev.cgss.core

import akka.actor.{Actor, Props, Status}
import akka.pattern.ask
import akka.util.Timeout
import dev.cgss.core.OrderParserSystem.{FailureResponse, ParseOrdersByDateRange, ParsedOrdersByDateRange}
import dev.cgss.core.loader.OrderLoader
import dev.cgss.core.parser.args.ArgsParser
import dev.cgss.core.parser.order.ParseOrder
import dev.cgss.date.DateRange

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

object OrderParserSystem {

  sealed trait Request

  sealed trait Response

  final case class ParseOrdersByDateRange(args: Array[String]) extends Request

  final case class FailureResponse(ex: String) extends Response

  final case class ParsedOrdersByDateRange(result: Seq[(DateRange, Int)]) extends Response

}

class OrderParserSystem extends Actor {
  private implicit val defaultTimeout: Timeout = 1.minute

  override def receive: Receive = {
    case ParseOrdersByDateRange(args) => {

      val s = sender()

      val argsParserActor = context.actorOf(Props[ArgsParser])
      val orderLoader = context.actorOf(Props[OrderLoader])

      val waitFor = for {
        parsedArgsResponse <- (argsParserActor ? ArgsParser.ArgsParseRequest(args)).mapTo[ArgsParser.Response]
        ordersResponse <- (orderLoader ? OrderLoader.LoadOrders).mapTo[OrderLoader.Response]
      } yield (parsedArgsResponse, ordersResponse)

      waitFor.onComplete {
        case Failure(exception) => s ! Status.Failure(exception)
        case Success(value) => {
          value._1 match {
            case ArgsParser.ParsedArgsResponse(parsedArgs) => value._2 match {
              case OrderLoader.LoadedOrders(orders) => {
                context.stop(argsParserActor)
                context.stop(orderLoader)

                val validOrders = orders
                  .filter(order => parsedArgs.orderDateRange contains order.creationDate)
                val futureSeq = parsedArgs
                  .intervals
                  .map(dateRange => context.actorOf(ParseOrder.props(dateRange, validOrders)))
                  .map(actor => (actor ? ParseOrder.StartProcess).mapTo[ParseOrder.ProcessedOrder])
                Future
                  .sequence(futureSeq)
                  .onComplete {
                    case Failure(exception) => s ! Status.Failure(exception)
                    case Success(value: Seq[ParseOrder.ProcessedOrder]) => {
                      val result = value.map(r => (r.range, r.count))
                      s ! ParsedOrdersByDateRange(result)
                    }
                  }
              }
            }
            case ArgsParser.ArgsParserFailure(ex) => s ! FailureResponse(ex)
          }
        }
      }
    }
  }
}
