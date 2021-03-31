package dev.cgss.generator

import dev.cgss.order.Item

import scala.util.Random

object ItemGenerator {

  def apply(n: Int = 100): Seq[Item] = if (n <= 0) {
    Seq.empty[Item]
  } else {
    Seq[Item](
      Item(
        cost = 1 + Random.nextInt(1000),
        fee = 1 + Random.nextInt(1000),
        tax = 1 + Random.nextInt(1000),
        product = ProductGenerator()
      )
    ) concat ItemGenerator(n - 1)
  }

}
