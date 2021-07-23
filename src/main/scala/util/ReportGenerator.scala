package util

import entity.DriveInfo

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, Month}
import scala.collection.immutable.List


class ReportGenerator(directoryPath: String, bikeStatsFilename: String, generalStatsFilename: String, usageStatsFilename: String) {

  private def getStatisticsWithNoErrorLines(list: List[Option[DriveInfo]]): List[Option[DriveInfo]] = {
    list.filterNot(_.equals(Option.empty))
  }

  private def countErrorParseLines(list: List[Option[DriveInfo]]): Int = {
    list.count(_.equals(Option.empty))
  }

  private def countOfTrips(list: List[Option[DriveInfo]]): Int = {
    list.size
  }

  private def countOfUsagesBetweenDates(startDate: LocalDateTime, endDate: LocalDateTime, list: List[Option[DriveInfo]]): Int = {
    list.count(usage =>
      (usage.get.startDate.isAfter(startDate) &&
        usage.get.startDate.isBefore(endDate)) ||
        (usage.get.endDate.isAfter(startDate) &&
          usage.get.endDate.isBefore(endDate))
    )
  }

  private def countOfUniqBicycleUsedBetweenDates(startDate: LocalDateTime, endDate: LocalDateTime, list: List[Option[DriveInfo]]): Int = {
    val drivesBetweenDates = list.filter(usage =>
      usage.get.startDate.isAfter(startDate) &&
        usage.get.startDate.isBefore(endDate) ||
        usage.get.endDate.isAfter(startDate) &&
          usage.get.endDate.isBefore(endDate)
    )
    drivesBetweenDates.filter(line => line.isInstanceOf[Option[DriveInfo]]).distinctBy(_.get.bikeNumber).size
  }

  private def longestDrive(list: List[Option[DriveInfo]]) = {
    val longestDrive = list.maxBy(_.get.duration)
    s"${longestDrive.get.duration}"
  }

  private def generateGeneralStatus(startDate: String, endDate: String, list: List[Option[DriveInfo]]): (String, String) = {
    val statisticsWithNoErrorLines = getStatisticsWithNoErrorLines(list)
    (directoryPath + generalStatsFilename,
      "\"Count of drives\"," + countOfTrips(statisticsWithNoErrorLines) +
        "\"\n\"Count of parse errors\",\"" + countErrorParseLines(list) +
        "\"\n\"Count of usages between dates\",\"" + countOfUsagesBetweenDates(parseTime(startDate), parseTime(endDate), statisticsWithNoErrorLines) +
        "\"\n\"Count of bicycles used between dates\",\"" + countOfUniqBicycleUsedBetweenDates(parseTime(startDate), parseTime(endDate), statisticsWithNoErrorLines) +
        "\"\n\"Longest drive\",\"" + longestDrive(statisticsWithNoErrorLines) + "\"")
  }

  private def generateMonthlyDrivesStatisticsReport(list: List[Option[DriveInfo]]): (String, String) = {
    val stats = Month.values().map(month => ("\"" + month + "\"", "\"" + monthlyDrivesStatistics(month, list) + "\""))
    val report = stats.mkString("\n")
    (directoryPath + usageStatsFilename, report.replaceAll("\\(", "").replaceAll("\\)", ""))
  }

  private def generateEachBicycleStatistics(list: List[Option[DriveInfo]]): (String, String) = {
    val stats = createStatisticsMapsForEachBicycle(list.groupBy(_.get.bikeNumber)).asInstanceOf[List[(String, Int, Int)]]
    val report = stats.sortBy(_._2)(Ordering.Int.reverse).mkString("\n")
    (directoryPath + bikeStatsFilename, report.replaceAll("\\(", "").replaceAll("\\)", ""))
  }

  private def monthlyDrivesStatistics(month: Month, list: List[Option[DriveInfo]]): Int = {
    list.count(line => line.get.startDate.getMonth.equals(month))
  }

  private def createStatisticsMapsForEachBicycle(map: Map[String, List[Option[DriveInfo]]]) = {
    map.map(tuple => (tuple._1, countOfDrives(tuple), totalDrivesDuration(tuple)))
  }

  private def countOfDrives(stats: (String, List[Option[DriveInfo]])): Int = {
    stats._2.size
  }

  private def totalDrivesDuration(stats: (String, List[Option[DriveInfo]])): Int = {
    stats._2.map(_.get.duration).sum
  }

  def generateReports(startDate: String, endDate: String, list: List[Option[DriveInfo]]): Array[(String, String)] = {
    val statisticsWithNoErrorLines = getStatisticsWithNoErrorLines(list)
    Array(
      generateGeneralStatus(startDate, endDate, list),
      generateMonthlyDrivesStatisticsReport(statisticsWithNoErrorLines),
      generateEachBicycleStatistics(statisticsWithNoErrorLines)
    )
  }

  private def parseTime(dateTime: String): LocalDateTime = {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    LocalDateTime.parse(dateTime, formatter)
  }
}
