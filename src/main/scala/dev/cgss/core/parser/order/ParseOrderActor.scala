package dev.cgss.core.parser.order

import akka.actor.{Actor, Props}
import dev.cgss.core.parser.order.ParseOrderActor.{ProcessedOrder, StartProcess}
import dev.cgss.date.DateRange
import dev.cgss.order.Order

object ParseOrderActor {

  def props(dateRange: DateRange, validOrders: Seq[Order]): Props = Props(new ParseOrderActor(dateRange, validOrders))

  sealed trait Request

  sealed trait Response

  final case class ProcessedOrder(range: DateRange, count: Int) extends Response

  final case object StartProcess extends Request

}

class ParseOrderActor(private val dateRange: DateRange, private val validOrders: Seq[Order]) extends Actor {
  override def receive: Receive = {
    case StartProcess => {
      val result = processOrder(dateRange, validOrders)
      sender() ! ProcessedOrder(result._1, result._2)
      context.stop(self)
    }
  }

  def processOrder(dateRange: DateRange, validOrders: Seq[Order]): (DateRange, Int) = {
    val orderCount = validOrders.count(order => isProductInDateRange(order, dateRange))

    (dateRange, orderCount)
  }

  private def isProductInDateRange(order: Order, dateRange: DateRange): Boolean = {
    order
      .itemList
      .exists(item => dateRange contains item.product.creationDate)
  }
}
