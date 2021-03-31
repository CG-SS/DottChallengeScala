package dev.cgss.generator

import dev.cgss.order.{Product => OrderProduct}

import java.time.LocalDateTime
import scala.util.Random

object ProductGenerator {

  private val randomName = Seq("Furadeira Parafusadeira Impacto 1/2'' 500w Titanium",
    "Jogo De Soquetes Sextavado C/chave Catraca 40pçs 4820 Fertak",
    "Placa de vídeo Nvidia Galax EX",
    "Placa de vídeo Nvidia Gainward Phoenix GeForce RTX 30",
    "Placa de vídeo Nvidia Gainward Ghost GeForce RTX 30 Series RTX 3060"
  )
  private val randomCategories = Seq("Jogos", "Eletronicos", "Ferramentas", "Roupas")

  def apply(): OrderProduct = OrderProduct(
    name = randomName(Random.nextInt(randomName.length)),
    category = randomCategories(Random.nextInt(randomCategories.length)),
    weight = 1 + Random.nextInt(10),
    price = 1 + Random.nextInt(10000),
    creationDate = LocalDateTime.now().minusDays(1 + Random.nextInt(10)).minusMonths(Random.nextInt(10)).minusYears(Random.nextInt(4))
  )


}
