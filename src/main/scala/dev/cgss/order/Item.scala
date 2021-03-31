package dev.cgss.order

import dev.cgss.order.{Product => OrderProduct}

final case class Item(
                       cost: BigDecimal,
                       fee: BigDecimal,
                       tax: BigDecimal,
                       product: OrderProduct
                     )
