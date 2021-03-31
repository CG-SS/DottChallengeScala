package dev.cgss.order

import java.time.LocalDateTime

final case class Product(
                               name: String,
                               category: String,
                               weight: BigDecimal,
                               price: BigDecimal,
                               creationDate: LocalDateTime
                             )
