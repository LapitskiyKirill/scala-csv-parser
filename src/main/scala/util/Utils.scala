package util

import entity.DriveInfo

import java.time.LocalDateTime
import scala.collection.immutable.List

object Utils {
  def getStatisticsWithNoErrorLines(list: List[Option[DriveInfo]]): List[Option[DriveInfo]] = {
    list.filterNot(_.isEmpty)
  }

  def isDateBetweenTwoAnother(date: LocalDateTime, startDate: LocalDateTime, endDate: LocalDateTime): Boolean = {
    (date.isAfter(startDate) && date.isBefore(endDate)) ||
      (date.isAfter(startDate) && date.isBefore(endDate))
  }
}
