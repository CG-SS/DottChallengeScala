package dev.cgss.core

import akka.actor.{Actor, ActorLogging, Props, Status}
import akka.pattern.ask
import akka.util.Timeout
import dev.cgss.core.OrderParserSystemActor.{FailureResponse, ParseOrdersByDateRange, ParsedOrdersByDateRange}
import dev.cgss.core.loader.OrderLoaderActor
import dev.cgss.core.parser.args.ArgsParserActor
import dev.cgss.core.parser.order.ParseOrderActor
import dev.cgss.date.DateRange

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

object OrderParserSystemActor {

  sealed trait Request

  sealed trait Response

  final case class ParseOrdersByDateRange(args: Array[String]) extends Request

  final case class FailureResponse(ex: String) extends Response

  final case class ParsedOrdersByDateRange(result: Seq[(DateRange, Int)]) extends Response

}

class OrderParserSystemActor extends Actor with ActorLogging {
  private implicit val defaultTimeout: Timeout = 1.minute

  override def receive: Receive = {
    case ParseOrdersByDateRange(args) => {

      log.debug(s"Received ParseOrdersByDateRange with args (${args.mkString(", ")})")

      val s = sender()

      val argsParserActor = context.actorOf(Props[ArgsParserActor])
      val orderLoader = context.actorOf(Props[OrderLoaderActor])

      val waitFor = for {
        parsedArgsResponse <- (argsParserActor ? ArgsParserActor.ArgsParseRequest(args)).mapTo[ArgsParserActor.Response]
        ordersResponse <- (orderLoader ? OrderLoaderActor.LoadOrders).mapTo[OrderLoaderActor.Response]
      } yield (parsedArgsResponse, ordersResponse)

      waitFor.onComplete {
        case Failure(exception) => s ! Status.Failure(exception)
        case Success(value) => {

          log.debug(s"Sucessfully parsed request")

          value._1 match {
            case ArgsParserActor.ParsedArgsResponse(parsedArgs) => value._2 match {
              case OrderLoaderActor.LoadedOrders(orders) => {
                context.stop(argsParserActor)
                context.stop(orderLoader)

                log.debug(s"ArgsParser and OrderLoader returned success with ${orders.length} orders")

                val validOrders = orders
                  .filter(order => parsedArgs.orderDateRange contains order.creationDate)
                val futureSeq = parsedArgs
                  .intervals
                  .map(dateRange => context.actorOf(ParseOrderActor.props(dateRange, validOrders)))
                  .map(actor => (actor ? ParseOrderActor.StartProcess).mapTo[ParseOrderActor.ProcessedOrder])
                Future
                  .sequence(futureSeq)
                  .onComplete {
                    case Failure(exception) => s ! Status.Failure(exception)
                    case Success(value: Seq[ParseOrderActor.ProcessedOrder]) => {
                      val result = value.map(r => (r.range, r.count))
                      s ! ParsedOrdersByDateRange(result)
                    }
                  }
              }
            }
            case ArgsParserActor.ArgsParserFailure(ex) => s ! FailureResponse(ex)
          }
        }
      }
    }
  }
}
