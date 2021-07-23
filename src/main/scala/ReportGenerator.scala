import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, Month}
import scala.collection.immutable.List


class ReportGenerator(directoryPath: String, bikeStatsFilename: String, generalStatsFilename: String, usageStatsFilename: String) {

  private def getStatisticsWithNoErrorLines(list: List[DriveInfo]): List[DriveInfo] = {
    list.filterNot(_.equals(new DriveInfo(Int.MinValue, LocalDateTime.MIN, LocalDateTime.MIN, Int.MinValue, "", Int.MinValue, "", "", "")))
  }

  private def countErrorParseLines(list: List[DriveInfo]): Int = {
    list.count(_.equals(new DriveInfo(Int.MinValue, LocalDateTime.MIN, LocalDateTime.MIN, Int.MinValue, "", Int.MinValue, "", "", "")))
  }

  private def countOfTrips(list: List[DriveInfo]): Int = {
    list.size
  }

  private def countOfBicycleUsedBetweenDates(startDate: LocalDateTime, endDate: LocalDateTime, list: List[DriveInfo]): Int = {
    list.count(a => (a.getStartDate.isAfter(startDate) && a.getStartDate.isBefore(endDate)) || (a.getEndDate.isAfter(startDate) && a.getEndDate.isBefore(endDate)))
  }

  private def countOfUniqBicycleUsedBetweenDates(startDate: LocalDateTime, endDate: LocalDateTime, list: List[DriveInfo]): Int = {
    val list1: List[Any] = for (line <- list) yield {
      if (line.getStartDate.isAfter(startDate) && line.getStartDate.isBefore(endDate) || line.getEndDate.isAfter(startDate) && line.getEndDate.isBefore(endDate)) {
        line
      }
    }
    val resultList = list1.filter(line => line.isInstanceOf[DriveInfo]).asInstanceOf[List[DriveInfo]]
    resultList.distinctBy(_.getBikeNumber).size
  }

  private def longestDrive(list: List[DriveInfo]) = {
    val longestDrive = list.maxBy(_.getDuration)
    s"${longestDrive.getDuration}"
  }

  private def generateGeneralStatus(startDate: String, endDate: String, list: List[DriveInfo]): (String, String) = {
    val statisticsWithNoErrorLines = getStatisticsWithNoErrorLines(list)
    (directoryPath + generalStatsFilename, "\"Count of drives\"," + countOfTrips(statisticsWithNoErrorLines) +
      "\"\n\"Count of parse errors\",\"" + countErrorParseLines(list) +
      "\"\n\"Count of bicycle\",\"" + countOfBicycleUsedBetweenDates(parseTime(startDate), parseTime(endDate), statisticsWithNoErrorLines) +
      "\"\n\"Count of uniq\",\"" + countOfUniqBicycleUsedBetweenDates(parseTime(startDate), parseTime(endDate), statisticsWithNoErrorLines) +
      "\"\n\"Longest drive\",\"" + longestDrive(statisticsWithNoErrorLines) + "\"")
  }

  private def generateMonthlyDrivesStatisticsReport(list: List[DriveInfo]): (String, String) = {
    val stats = for (month <- Month.values()) yield {
      ("\"" + month + "\"", "\"" + monthlyDrivesStatistics(month, list) + "\"")
    }
    val report = stats.mkString("\n")
    (directoryPath + usageStatsFilename, report.replaceAll("\\(", "").replaceAll("\\)", ""))
  }

  private def monthlyDrivesStatistics(month: Month, list: List[DriveInfo]): Int = {
    list.count(line => line.getStartDate.getMonth.equals(month))
  }

  private def generateEachBicycleStatistics(list: List[DriveInfo]): (String, String) = {
    val eachBicycleStats = list.groupBy(_.getBikeNumber)
    val stats = createStatisticsMapsForEachBicycle(eachBicycleStats).asInstanceOf[List[(String, Int, Int)]]
    val report = stats.sortBy(_._2)(Ordering.Int.reverse).mkString("\n")
    (directoryPath + bikeStatsFilename, report.replaceAll("\\(", "").replaceAll("\\)", ""))
  }

  private def createStatisticsMapsForEachBicycle(map: Map[String, List[DriveInfo]]) = {
    val stats = for (tuple <- map) yield {
      Tuple3(tuple._1, countOfDrives(tuple), totalDrivesDuration(tuple))
    }
    stats
  }

  private def countOfDrives(stats: (String, List[DriveInfo])): Int = {
    stats._2.size
  }

  private def totalDrivesDuration(stats: (String, List[DriveInfo])): Int = {
    durationSum(stats._2.map(_.getDuration))
  }

  def durationSum(list: List[Int]): Int = {
    list match {
      case sum :: tail => sum + durationSum(tail)
      case Nil => 0
    }
  }

  def generateReports(startDate: String, endDate: String, list: List[DriveInfo]): Array[(String, String)] = {
    val statisticsWithNoErrorLines = getStatisticsWithNoErrorLines(list)
    Array(generateGeneralStatus(startDate, endDate, list), generateMonthlyDrivesStatisticsReport(statisticsWithNoErrorLines), generateEachBicycleStatistics(statisticsWithNoErrorLines))
  }

  private def parseTime(dateTime: String): LocalDateTime = {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    LocalDateTime.parse(dateTime, formatter)
  }
}
