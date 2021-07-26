package reportGenerator

import entity.{DriveInfo, Report}
import util.Utils

import scala.collection.parallel.CollectionConverters._
import scala.collection.immutable.List

class BikeStatsReportGenerator(directoryPath: String, bikeStatsFilename: String) extends ReportGenerator {
  override def generate(list: List[Option[DriveInfo]]): Report = {
    val statisticsWithNoErrorLines = Utils.getStatisticsWithNoErrorLines(list)
    generateEachBicycleStatistics(statisticsWithNoErrorLines)
  }

  private def generateEachBicycleStatistics(list: List[Option[DriveInfo]]): Report = {
    val stats = createStatisticsMapsForEachBicycle(list.groupBy(_.get.bikeNumber)).asInstanceOf[List[(String, Int, Int)]]
    val report = stats.sortBy(_._2)(Ordering.Int.reverse).mkString("\n")
    Report(directoryPath + bikeStatsFilename, report.replaceAll("\\(", "").replaceAll("\\)", ""))
  }

  private def createStatisticsMapsForEachBicycle(map: Map[String, List[Option[DriveInfo]]]) = {
    map.map(tuple => (tuple._1, countOfDrives(tuple), totalDrivesDuration(tuple)))
  }

  private def countOfDrives(stats: (String, List[Option[DriveInfo]])): Int = {
    stats._2.size
  }

  private def totalDrivesDuration(stats: (String, List[Option[DriveInfo]])): Int = {
    stats._2.par.map(_.get.duration).sum
  }
}