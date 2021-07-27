package reportGenerator

import entity.{DateRange, DriveInfo, Report}
import util.Utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.collection.immutable.List
import scala.collection.parallel.CollectionConverters._

class GeneralStatsReportGenerator(dateRange: DateRange, directoryPath: String, generalStatsFilename: String) {
  def generate(list: List[Option[DriveInfo]]): (Int, Int, Int, Int, Int) = {
    generateGeneralStatus(dateRange, list)
  }

  private def countErrorParseLines(list: List[Option[DriveInfo]]): Int = {
    list.par.count(_.equals(Option.empty))
  }

  private def countOfTrips(list: List[Option[DriveInfo]]): Int = {
    list.size
  }

  private def countOfUsagesBetweenDates(startDate: LocalDateTime, endDate: LocalDateTime, list: List[Option[DriveInfo]]): Int = {
    list.par.count(usage => Utils.isDateBetweenTwoAnother(usage.get.startDate, startDate, endDate) || Utils.isDateBetweenTwoAnother(usage.get.endDate, startDate, endDate))
  }

  private def countOfUniqBicycleUsedBetweenDates(startDate: LocalDateTime, endDate: LocalDateTime, list: List[Option[DriveInfo]]): Int = {
    val drivesBetweenDates = list.filter(usage => Utils.isDateBetweenTwoAnother(usage.get.startDate, startDate, endDate) || Utils.isDateBetweenTwoAnother(usage.get.endDate, startDate, endDate))
    drivesBetweenDates.filter(line => line.isInstanceOf[Option[DriveInfo]]).distinctBy(_.get.bikeNumber).size
  }

  private def longestDrive(list: List[Option[DriveInfo]]) = {
    list.maxBy(_.get.duration).map(_.duration)
  }

  def generateReport(tuple: (Int, Int, Int, Int, Int)): Report = {
    Report(directoryPath + generalStatsFilename,
      "\"Count of drives\",\"" + tuple._1 +
        "\"\n\"Count of parse errors\",\"" + tuple._2 +
        "\"\n\"Count of usages between dates\",\"" + tuple._3 +
        "\"\n\"Count of bicycles used between dates\",\"" + tuple._4 +
        "\"\n\"Longest drive\",\"" + tuple._5 + "\"")
  }

  private def generateGeneralStatus(dateRange: DateRange, list: List[Option[DriveInfo]]): (Int, Int, Int, Int, Int) = {
    val statisticsWithNoErrorLines = Utils.getStatisticsWithNoErrorLines(list)
    (countOfTrips(statisticsWithNoErrorLines),
      countErrorParseLines(list),
      countOfUsagesBetweenDates(parseTime(dateRange.startDate), parseTime(dateRange.endDate), statisticsWithNoErrorLines),
      countOfUniqBicycleUsedBetweenDates(parseTime(dateRange.startDate), parseTime(dateRange.endDate), statisticsWithNoErrorLines),
      longestDrive(statisticsWithNoErrorLines).getOrElse(0))
  }

  private def parseTime(dateTime: String): LocalDateTime = {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    LocalDateTime.parse(dateTime, formatter)
  }
}
