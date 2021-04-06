package dev.cgss.core.loader

import akka.actor.{Actor, ActorLogging}
import dev.cgss.core.loader.OrderLoaderActor.{LoadOrders, LoadedOrders}
import dev.cgss.generator.OrderGenerator
import dev.cgss.order.Order

object OrderLoaderActor {

  sealed trait Request

  sealed trait Response

  final case class LoadedOrders(orders: Seq[Order]) extends Response

  final case object LoadOrders extends Request

}

class OrderLoaderActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case LoadOrders => {
      log.debug("Loading orders")
      sender() ! LoadedOrders(OrderGenerator())
    }
  }
}
