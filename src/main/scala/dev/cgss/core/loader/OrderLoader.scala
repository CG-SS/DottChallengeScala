package dev.cgss.core.loader

import akka.actor.{Actor, Status}
import dev.cgss.core.loader.OrderLoader.{LoadOrders, LoadedOrders}
import dev.cgss.generator.OrderGenerator
import dev.cgss.order.Order

object OrderLoader {

  sealed trait Request

  sealed trait Response

  final case class LoadedOrders(orders: Seq[Order]) extends Response

  final case object LoadOrders extends Request

}

class OrderLoader extends Actor {
  override def receive: Receive = {
    case LoadOrders => sender() ! LoadedOrders(OrderGenerator())
  }
}
