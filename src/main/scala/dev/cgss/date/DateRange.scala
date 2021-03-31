package dev.cgss.date

import java.time.LocalDateTime

case class DateRange(beginDate: LocalDateTime, endDate: LocalDateTime, source: String = "") {

  def contains(date: LocalDateTime): Boolean = date.isAfter(beginDate.minusDays(1)) && date.isBefore(endDate.plusDays(1))
}
