package reportGenerator

import entity.{BikeReport, DriveInfo, Report}
import util.Utils

import scala.collection.parallel.CollectionConverters._
import scala.collection.immutable.List
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


class BikeStatsReportGenerator(directoryPath: String, bikeStatsFilename: String) {
  def generate(list: Future[List[Option[DriveInfo]]]): Future[List[BikeReport]] = {
    val statisticsWithNoErrorLines = Utils.getStatisticsWithNoErrorLines(list)
    generateEachBicycleStatistics(statisticsWithNoErrorLines)
  }

  private def generateEachBicycleStatistics(list: Future[List[Option[DriveInfo]]]): Future[List[BikeReport]] = {
    createStatisticsMapsForEachBicycle(list.map(_.groupBy(_.get.bikeNumber))).asInstanceOf[Future[List[BikeReport]]]
  }

  def generateReport(stats: Future[List[BikeReport]]): Future[Report] = {
    val futureReport = stats.map(_.sortBy(_.countOfDrives)(Ordering.Int.reverse).mkString("\n"))
    futureReport.map(report => Report(directoryPath + bikeStatsFilename, report.replaceAll("\\(", "").replaceAll("\\)", "")))
  }

  private def createStatisticsMapsForEachBicycle(map: Future[Map[String, List[Option[DriveInfo]]]]) = {
    map.map(a => a.map(tuple => BikeReport(tuple._1, countOfDrives(tuple), totalDrivesDuration(tuple))))
  }

  private def countOfDrives(stats: (String, List[Option[DriveInfo]])): Int = {
    stats._2.size
  }

  private def totalDrivesDuration(stats: (String, List[Option[DriveInfo]])): Int = {
    stats._2.par.map(_.get.duration).sum
  }
}