package dev.cgss.generator

import dev.cgss.order.Order

import java.time.LocalDateTime
import scala.util.Random

object OrderGenerator {

  private val randomNames = Seq("Andrew", "Michael", "John", "Carl")
  private val randomContacts = Seq("(154) 3444-3829", "(111) 3444-3829", "(101) 3214-5743", "(014) 3444-5431")
  private val randomAddresses = Seq("R. Lusíadas 10-12, 3080-251 Figueira da Foz, Portugal", "N109-8, Figueira da Foz, Portugal")

  def apply(n: Int = 1000): Seq[Order] = if (n <= 0) {
    Seq.empty[Order]
  } else {
    val orderItems = ItemGenerator()
    val totalCost: BigDecimal = orderItems
      .map(item => item.cost)
      .sum

    Seq[Order](
      Order(
        customerName = randomNames(Random.nextInt(randomNames.length)),
        contact = randomContacts(Random.nextInt(randomContacts.length)),
        address = randomAddresses(Random.nextInt(randomAddresses.length)),
        total = totalCost,
        creationDate = LocalDateTime.now().minusDays(Random.nextInt(10)).minusMonths(Random.nextInt(10)).minusYears(Random.nextInt(4)),
        itemList = orderItems)
    ) concat OrderGenerator(n - 1)
  }


}
