package dev.cgss.order

import java.time.LocalDateTime

final case class Order(
                  customerName: String,
                  contact: String,
                  address: String,
                  total: BigDecimal,
                  creationDate: LocalDateTime,
                  itemList: Seq[Item]
                )
