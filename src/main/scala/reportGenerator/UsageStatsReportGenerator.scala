package reportGenerator

import entity.{DriveInfo, Report, UsageReport}
import util.Utils

import java.time.Month
import scala.collection.immutable.List
import scala.collection.parallel.CollectionConverters._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


class UsageStatsReportGenerator(directoryPath: String, usageStatsFilename: String) {
  def generate(list: Future[List[Option[DriveInfo]]]): Future[List[UsageReport]] = {
    val statisticsWithNoErrorLines = Utils.getStatisticsWithNoErrorLines(list)
    generateMonthlyDrivesStatisticsReport(statisticsWithNoErrorLines)
  }

  def generateReport(stats: Future[List[UsageReport]]): Future[Report] = {
    val report = stats.map(_.mkString("\n"))
    report.map(rep => Report(directoryPath + usageStatsFilename, rep.replaceAll("UsageReport\\(", "").replaceAll("\\)", "")))
  }

  private def generateMonthlyDrivesStatisticsReport(list: Future[List[Option[DriveInfo]]]): Future[List[UsageReport]] = {
    Future.sequence(Month.values()
      .map(month => monthlyDrivesStatistics(month, list)
        .map(monthlyDrivesStatistics => UsageReport("\"" + month + "\"", monthlyDrivesStatistics))
      ).toList)
  }

  private def monthlyDrivesStatistics(month: Month, list: Future[List[Option[DriveInfo]]]): Future[Int] = {
    list.map(_.par.count(_.get.startDate.getMonth.equals(month)))
  }
}
