package util

import entity.DriveInfo

import java.time.LocalDateTime
import scala.collection.immutable.List
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


object Utils {
  def getStatisticsWithNoErrorLines(list: Future[List[Option[DriveInfo]]]): Future[List[Option[DriveInfo]]] = {
    list.map(_.filterNot(_.isEmpty))
  }

  def isDateBetweenTwoAnother(date: LocalDateTime, startDate: LocalDateTime, endDate: LocalDateTime): Boolean = {
    (date.isAfter(startDate) && date.isBefore(endDate)) ||
      (date.isAfter(startDate) && date.isBefore(endDate))
  }
}
