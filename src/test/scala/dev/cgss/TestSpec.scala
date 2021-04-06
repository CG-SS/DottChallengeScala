package dev.cgss

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import dev.cgss.core.loader.OrderLoaderActor
import dev.cgss.core.parser.args.ArgsParserActor
import dev.cgss.core.parser.order.ParseOrderActor
import dev.cgss.date.DateParser
import dev.cgss.generator.OrderGenerator
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

class TestSpec extends TestKit(ActorSystem("TestSystem"))
  with ImplicitSender
  with AnyWordSpecLike
  with BeforeAndAfterAll {

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  "An ArgsParserActor" should {
    "Parse args" in {
      implicit val timeout: Timeout = 1.minute
      val argsParserActor = system.actorOf(Props[ArgsParserActor])
      val args = Array("2018-01-01 00:00:00", "2019-01-01 00:00:00")

      (argsParserActor ? ArgsParserActor.ArgsParseRequest(args))
        .mapTo[ArgsParserActor.Response]
        .onComplete {
          case Failure(_) => fail("Should've parsed successfully!")
          case Success(value) => value match {
            case ArgsParserActor.ParsedArgsResponse(_) => succeed
            case ArgsParserActor.ArgsParserFailure(_) => fail("Should've parsed successfully!")
          }
        }
    }

    "Parse args with custom intervals" in {
      implicit val timeout: Timeout = 1.minute
      val argsParserActor = system.actorOf(Props[ArgsParserActor])
      val args = Array("2018-01-01 00:00:00", "2019-01-01 00:00:00", """("1-3", "4-6", "7-12", ">12")""")

      (argsParserActor ? ArgsParserActor.ArgsParseRequest(args))
        .mapTo[ArgsParserActor.Response]
        .onComplete {
          case Failure(_) => fail("Should've parsed successfully!")
          case Success(value) => value match {
            case ArgsParserActor.ParsedArgsResponse(_) => succeed
            case ArgsParserActor.ArgsParserFailure(_) => fail("Should've parsed successfully!")
          }
        }
    }

    "Return failure for illegal args" in {
      implicit val timeout: Timeout = 1.minute
      val argsParserActor = system.actorOf(Props[ArgsParserActor])
      val args = Array("2018-01-01 00:00:00")

      (argsParserActor ? ArgsParserActor.ArgsParseRequest(args))
        .mapTo[ArgsParserActor.Response]
        .onComplete {
          case Failure(_) => fail("Should've failed to parse but without exception!")
          case Success(value) => value match {
            case ArgsParserActor.ParsedArgsResponse(_) => fail("Should've failed to parse!")
            case ArgsParserActor.ArgsParserFailure(_) => succeed
          }
        }
    }

  }

  "An OrderLoaderActor" should {
    "Load orders" in {
      implicit val timeout: Timeout = 1.minute
      val orderLoaderActor = system.actorOf(Props[OrderLoaderActor])

      (orderLoaderActor ? OrderLoaderActor.LoadOrders)
        .mapTo[OrderLoaderActor.Response]
        .onComplete {
          case Failure(_) => fail("Should've loaded sucessfully!")
          case Success(value) => value match {
            case OrderLoaderActor.LoadedOrders(_) => succeed
          }
        }
    }
  }

  "An ParseOrderActor" should {
    "Parse and order within an interval" in {
      implicit val timeout: Timeout = 1.minute
      val parseOrderActor = system.actorOf(ParseOrderActor.props(DateParser.fromDateRange("1-3"), OrderGenerator()))

      (parseOrderActor ? ParseOrderActor.StartProcess)
        .mapTo[ParseOrderActor.Response]
        .onComplete {
          case Failure(_) => fail("Should've loaded sucessfully!")
          case Success(value) => value match {
            case ParseOrderActor.ProcessedOrder(_, _) => succeed
          }
        }
    }
  }

}
