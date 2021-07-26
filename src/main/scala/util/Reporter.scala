package util

import entity.{DateRange, DriveInfo, Report}
import reportGenerator.{BikeStatsReportGenerator, GeneralStatsReportGenerator, ReportGenerator, UsageStatsReportGenerator}

import scala.collection.immutable.List


class Reporter(directoryPath: String, bikeStatsFilename: String, generalStatsFilename: String, usageStatsFilename: String) {

  def generateReports(dateRange: DateRange, list: List[Option[DriveInfo]]): List[Report] = {
    val reporters = List[ReportGenerator](
      new BikeStatsReportGenerator(directoryPath, bikeStatsFilename),
      new UsageStatsReportGenerator(directoryPath, usageStatsFilename),
      new GeneralStatsReportGenerator(dateRange, directoryPath, generalStatsFilename)
    )
    reporters.map(reporter => reporter.generate(list))
  }
}
