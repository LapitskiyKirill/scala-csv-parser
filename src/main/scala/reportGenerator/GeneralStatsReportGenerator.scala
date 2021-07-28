package reportGenerator

import entity.{DateRange, DriveInfo, GeneralReport, Report}
import util.Utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.collection.immutable.List
import scala.collection.parallel.CollectionConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GeneralStatsReportGenerator(dateRange: DateRange, directoryPath: String, generalStatsFilename: String) {
  def generate(list: Future[List[Option[DriveInfo]]]): Future[GeneralReport] = {
    generateGeneralStatus(dateRange, list)
  }

  private def countErrorParseLines(list: Future[List[Option[DriveInfo]]]): Future[Int] = {
    list.map(_.par.count(_.equals(Option.empty)))
  }

  private def countOfTrips(list: Future[List[Option[DriveInfo]]]): Future[Int] = {
    list.map(_.size)
  }

  private def countOfUsagesBetweenDates(startDate: LocalDateTime, endDate: LocalDateTime, list: Future[List[Option[DriveInfo]]]): Future[Int] = {
    list.map(_.par.count(usage => Utils.isDateBetweenTwoAnother(usage.get.startDate, startDate, endDate) || Utils.isDateBetweenTwoAnother(usage.get.endDate, startDate, endDate)))
  }

  private def countOfUniqBicycleUsedBetweenDates(startDate: LocalDateTime, endDate: LocalDateTime, list: Future[List[Option[DriveInfo]]]): Future[Int] = {
    val drivesBetweenDates = list.map(_.filter(usage => Utils.isDateBetweenTwoAnother(usage.get.startDate, startDate, endDate) || Utils.isDateBetweenTwoAnother(usage.get.endDate, startDate, endDate)))
    drivesBetweenDates.map(_.filter(_.isInstanceOf[Option[DriveInfo]]).distinctBy(_.get.bikeNumber).size)
  }

  private def longestDrive(list: Future[List[Option[DriveInfo]]]): Future[Option[Int]] = {
    list.map(_.maxBy(_.get.duration).map(_.duration))
  }

  def generateReport(generalReport: Future[GeneralReport]): Future[Report] = {
    generalReport.map(report => Report(directoryPath + generalStatsFilename,
      "\"Count of drives\",\"" + report.countOfDrives +
        "\"\n\"Count of parse errors\",\"" + report.countOfParseErrors +
        "\"\n\"Count of usages between dates\",\"" + report.countOfUsagesBetweenDates +
        "\"\n\"Count of bicycles used between dates\",\"" + report.countOfUsagesBetweenDates +
        "\"\n\"Longest drive\",\"" + report.longestDrive + "\""))
  }

  private def generateGeneralStatus(dateRange: DateRange, list: Future[List[Option[DriveInfo]]]): Future[GeneralReport] = {
    val statisticsWithNoErrorLines = Utils.getStatisticsWithNoErrorLines(list)
    val future = Future.sequence(List(countOfTrips(statisticsWithNoErrorLines),
      countErrorParseLines(list),
      countOfUsagesBetweenDates(parseTime(dateRange.startDate), parseTime(dateRange.endDate), statisticsWithNoErrorLines),
      countOfUniqBicycleUsedBetweenDates(parseTime(dateRange.startDate), parseTime(dateRange.endDate), statisticsWithNoErrorLines),
      longestDrive(statisticsWithNoErrorLines).map(_.getOrElse(0))))
    future.map(fut => {
      val iterator = fut.iterator
      GeneralReport(iterator.next(), iterator.next(), iterator.next(), iterator.next(), iterator.next())
    })
  }

  private def parseTime(dateTime: String): LocalDateTime = {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    LocalDateTime.parse(dateTime, formatter)
  }
}
