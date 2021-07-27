package reportGenerator

import entity.{DriveInfo, Report}
import util.Utils

import java.time.Month
import scala.collection.immutable.List
import scala.collection.parallel.CollectionConverters._


class UsageStatsReportGenerator(directoryPath: String, usageStatsFilename: String) {
  def generate(list: List[Option[DriveInfo]]): Array[(String, Int)] = {
    val statisticsWithNoErrorLines = Utils.getStatisticsWithNoErrorLines(list)
    generateMonthlyDrivesStatisticsReport(statisticsWithNoErrorLines)
  }

  def generateReport(stats: Array[(String, Int)]): Report = {
    val report = stats.mkString("\n")
    Report(directoryPath + usageStatsFilename, report.replaceAll("\\(", "").replaceAll("\\)", ""))
  }

  private def generateMonthlyDrivesStatisticsReport(list: List[Option[DriveInfo]]): Array[(String, Int)] = {
    Month.values().map(month => ("\"" + month + "\"", + monthlyDrivesStatistics(month, list)  ))
  }

  private def monthlyDrivesStatistics(month: Month, list: List[Option[DriveInfo]]): Int = {
    list.par.count(line => line.get.startDate.getMonth.equals(month))
  }
}
