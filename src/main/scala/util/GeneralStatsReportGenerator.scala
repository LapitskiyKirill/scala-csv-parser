package util

import entity.{DateRange, DriveInfo, Report}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.collection.immutable.List

class GeneralStatsReportGenerator(dateRange: DateRange, directoryPath: String, generalStatsFilename: String) extends ReportGenerator {
  override def generate(list: List[Option[DriveInfo]]): Report = {
    generateGeneralStatus(dateRange, list)
  }

  private def countErrorParseLines(list: List[Option[DriveInfo]]): Int = {
    list.count(_.equals(Option.empty))
  }

  private def countOfTrips(list: List[Option[DriveInfo]]): Int = {
    list.size
  }

  private def countOfUsagesBetweenDates(startDate: LocalDateTime, endDate: LocalDateTime, list: List[Option[DriveInfo]]): Int = {
    list.count(usage => Utils.isDateBetweenTwoAnother(usage.get.startDate, startDate, endDate) || Utils.isDateBetweenTwoAnother(usage.get.endDate, startDate, endDate))
  }

  private def countOfUniqBicycleUsedBetweenDates(startDate: LocalDateTime, endDate: LocalDateTime, list: List[Option[DriveInfo]]): Int = {
    val drivesBetweenDates = list.filter(usage => Utils.isDateBetweenTwoAnother(usage.get.startDate, startDate, endDate) || Utils.isDateBetweenTwoAnother(usage.get.endDate, startDate, endDate))
    drivesBetweenDates.filter(line => line.isInstanceOf[Option[DriveInfo]]).distinctBy(_.get.bikeNumber).size
  }

  private def longestDrive(list: List[Option[DriveInfo]]) = {
    val longestDrive = list.maxBy(_.get.duration)
    s"${longestDrive.get.duration}"
  }

  private def generateGeneralStatus(dateRange: DateRange, list: List[Option[DriveInfo]]): Report = {
    val statisticsWithNoErrorLines = Utils.getStatisticsWithNoErrorLines(list)
    Report(directoryPath + generalStatsFilename,
      "\"Count of drives\"," + countOfTrips(statisticsWithNoErrorLines) +
        "\"\n\"Count of parse errors\",\"" + countErrorParseLines(list) +
        "\"\n\"Count of usages between dates\",\"" + countOfUsagesBetweenDates(parseTime(dateRange.startDate), parseTime(dateRange.endDate), statisticsWithNoErrorLines) +
        "\"\n\"Count of bicycles used between dates\",\"" + countOfUniqBicycleUsedBetweenDates(parseTime(dateRange.startDate), parseTime(dateRange.endDate), statisticsWithNoErrorLines) +
        "\"\n\"Longest drive\",\"" + longestDrive(statisticsWithNoErrorLines) + "\"")
  }

  private def parseTime(dateTime: String): LocalDateTime = {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    LocalDateTime.parse(dateTime, formatter)
  }
}
