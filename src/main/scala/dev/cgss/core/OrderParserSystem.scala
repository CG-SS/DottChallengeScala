package dev.cgss.core

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.util.Timeout
import dev.cgss.core.parser.args.{ArgsParser, ParsedArgs}
import dev.cgss.date.DateRange
import dev.cgss.generator.OrderGenerator
import dev.cgss.order.Order

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

object OrderParserSystem {

  def apply(): Behavior[Request] =
    Behaviors.setup[Request] { context =>
      implicit val timeout: Timeout = 1.minute
      val argsParserActor: ActorRef[ArgsParser.Request] = context.spawn(ArgsParser(), "args-parser")

      Behaviors.receiveMessage[Request] {
        case ParseArgsRequest(args, replyTo) => {

          context.ask(argsParserActor, ref => ArgsParser.ParseArgsRequest(args, ref)) {
            case Failure(exception) => {
              replyTo ! WrongArgs(exception.getMessage)
              RequestFailed()
            }
            case Success(value) => value match {
              case ArgsParser.ParsedArgsResponse(parseArgs) => ParseParsedArgsRequest(parseArgs, replyTo)
              case ArgsParser.WrongArgsResponse(ex) => {
                replyTo ! WrongArgs(ex)
                RequestFailed()
              }
            }
          }

          Behaviors.same[Request]
        }
        case ParseParsedArgsRequest(parsedArgs, replyTo) => {

          val validOrders = OrderGenerator()
            .filter(order => parsedArgs.orderDateRange contains order.creationDate)
          val parsedIntervals = parsedArgs
            .intervals
            .map(dateRange => processOrder(dateRange, validOrders))
          Future
            .sequence(parsedIntervals)
            .onComplete {
              case Failure(ex) => replyTo ! WrongArgs(ex.getMessage)
              case Success(seq) => replyTo ! NumberOfOrdersByTimeRange(seq)
            }

          Behaviors.stopped[Request]
        }
        case RequestFailed() => Behaviors.stopped[Request]
      }
    }


  def processOrder(dateRange: DateRange, validOrders: Seq[Order]): Future[(DateRange, Int)] = Future {
    val orderCount = validOrders.count(order => isProductInDateRange(order, dateRange))

    (dateRange, orderCount)
  }

  private def isProductInDateRange(order: Order, dateRange: DateRange): Boolean = {
    order
      .itemList
      .exists(item => dateRange contains item.product.creationDate)
  }

  sealed trait Request

  sealed trait Response

  final case class ParseArgsRequest(args: Array[String], actorRef: ActorRef[Response]) extends Request

  final case class ParseParsedArgsRequest(parsedArgs: ParsedArgs, actorRef: ActorRef[Response]) extends Request

  final case class RequestFailed() extends Request

  final case class WrongArgs(msg: String) extends Response

  final case class NumberOfOrdersByTimeRange(orders: Seq[(DateRange, Int)]) extends Response

}
