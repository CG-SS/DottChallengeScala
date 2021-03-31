package dev.cgss.date

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateParser {

  private val startDate = LocalDateTime.now
  private val defaultFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  def fromDate(dateStr: String): LocalDateTime = LocalDateTime.parse(dateStr, defaultFormatter)

  def fromDateRange(dateRangeStr: String): DateRange = {
    if (dateRangeStr == null || dateRangeStr.isBlank) throw new DateException("Date range string cannot be null or blank!")

    if (!dateRangeStr.head.isDigit) {
      parseInfiniteDateRange(dateRangeStr)
    } else {
      parseFiniteDateRange(dateRangeStr)
    }
  }

  private def parseFiniteDateRange(dateRangeStr: String): DateRange = {
    val startMonth = dateRangeStr.takeWhile(c => c.isDigit).toLong
    val endMonth = dateRangeStr.dropWhile(c => !(c equals '-')).drop(1).toLong

    DateRange(startDate.minusMonths(endMonth), startDate.minusMonths(startMonth), dateRangeStr)
  }

  private def parseInfiniteDateRange(dateRangeStr: String): DateRange = {
    val symbol = dateRangeStr.head
    val months = dateRangeStr.drop(1).toLong

    if (symbol equals '>') {
      DateRange(LocalDateTime.MIN.plusDays(1), startDate.minusMonths(months), dateRangeStr)
    } else if (symbol equals '<') {
      DateRange(startDate.minusMonths(months), startDate, dateRangeStr)
    } else {
      throw new DateException(s"Unknown symbol $symbol")
    }
  }
}
