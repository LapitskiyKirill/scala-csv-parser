import com.typesafe.config.ConfigFactory
import entity.{DateRange, DriveInfo, Report}
import io.{ParameterizedReader, Writer}
import mapper.DriveMapper
import reportGenerator.{BikeStatsReportGenerator, GeneralStatsReportGenerator, UsageStatsReportGenerator}
import util.Reporter
import validator.DriveValidator

import scala.collection.immutable.Seq
import scala.collection.parallel.CollectionConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

object Main {

  def execute(): Unit = {
    val config = ConfigFactory.load
    val path = config.getString("filename.path")
    val sourceFilenames = config.getString("filename.source")
    val sourceFilenamesList: List[String] = sourceFilenames.split(",").toList
    val sourceFilenamesListWithPath = sourceFilenamesList.map(path + _)
    val bikeFilename = config.getString("filename.bike")
    val generalFilename = config.getString("filename.general")
    val usageFilename = config.getString("filename.usage")
    val reader = new ParameterizedReader[DriveInfo](DriveValidator, DriveMapper)
    val reporter = Reporter(path, bikeFilename, generalFilename, usageFilename)
    val reportsMonad = processFile(reader, reporter, sourceFilenamesListWithPath)
    Await.ready(reportsMonad, 40000 millis)
    reportsMonad.map(reports => Writer.write(reports))
    Thread.sleep(1000)
  }

  def main(args: Array[String]): Unit = {
    val start = System.nanoTime
    execute()
    val end = System.nanoTime
    print((end - start) / 1000000)
  }

  def processFile(reader: ParameterizedReader[DriveInfo], reporter: Reporter, sourceFilenamesListWithPath: List[String]): Future[List[Report]] = {
    val bikeStatsReportGenerator = new BikeStatsReportGenerator(reporter.directoryPath, reporter.bikeStatsFilename)
    val usageStatsReportGenerator = new UsageStatsReportGenerator(reporter.directoryPath, reporter.usageStatsFilename)
    val generalStatsReportGenerator = new GeneralStatsReportGenerator(DateRange("2010-09-20 12:26:08", "2010-10-26 12:26:08"), reporter.directoryPath, reporter.generalStatsFilename)
    val result: Future[List[Report]] = {
      val reports: Seq[(List[(String, Int, Int)], Array[(String, Int)], (Int, Int, Int, Int, Int))] = sourceFilenamesListWithPath.par.map(
        sourceFilenameListWithPath => {
          val lines = reader.readFile(sourceFilenameListWithPath)
          (bikeStatsReportGenerator.generate(lines), usageStatsReportGenerator.generate(lines), generalStatsReportGenerator.generate(lines))
        }).seq
      val bikeStats = reports.flatMap(_._1).groupBy(_._1).map(map => (map._1, map._2.map(_._2).sum, map._2.map(_._3).sum)).toList
      val usageStats = reports.flatMap(_._2).groupBy(_._1).map(map => (map._1, map._2.map(_._2).sum)).toArray
      val generalStatsList = reports.map(_._3)
      val generalStats = (generalStatsList.map(_._1).sum, generalStatsList.map(_._2).sum, generalStatsList.map(_._3).sum, generalStatsList.map(_._4).sum, generalStatsList.map(_._5).sum)
      Future(List(bikeStatsReportGenerator.generateReport(bikeStats), usageStatsReportGenerator.generateReport(usageStats), generalStatsReportGenerator.generateReport(generalStats)))
    }
    result
  }
}