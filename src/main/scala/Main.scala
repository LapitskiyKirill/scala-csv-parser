import com.typesafe.config.ConfigException.Null
import com.typesafe.config.ConfigFactory
import entity.{BikeReport, DataBase, DateRange, Drive, DriveInfo, GeneralReport, Report, Station, Tables, UsageReport}
import io.{ParameterizedReader, Writer}
import mapper.DriveMapper
import reportGenerator.{BikeStatsReportGenerator, GeneralStatsReportGenerator, UsageStatsReportGenerator}
import util.Reporter
import validator.DriveValidator

import scala.collection.immutable.Seq
import scala.collection.parallel.CollectionConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

object Main {

  def execute(): Unit = {
    val config = ConfigFactory.load
    val path = config.getString("filename.path")
    val sourceFilenames = config.getString("filename.source")
    val sourceFilenamesList: List[String] = sourceFilenames.split(",").toList
    val sourceFilenamesListWithPath = sourceFilenamesList.map(path + _)
    //    val bikeFilename = config.getString("filename.bike")
    //    val generalFilename = config.getString("filename.general")
    //    val usageFilename = config.getString("filename.usage")
    val reader = new ParameterizedReader[DriveInfo](DriveValidator, DriveMapper)
    //    val reporter = Reporter(path, bikeFilename, generalFilename, usageFilename)
    //    val reportsMonad = processAll(reader, reporter, sourceFilenamesListWithPath)
    //    val a = reportsMonad.map(Writer.write)
    //    Await.ready(a, Duration.Inf)
    saveEntities(reader, sourceFilenamesListWithPath.head)
    val a = readEntities()
    Await.ready(a, Duration.Inf)
  }

  def readEntities(): Future[Seq[Drive]] = {
    val a = DataBase.db.run[Seq[Drive]](Tables.drives.result)
    Await.ready(a, Duration.Inf)
    println(a.map(b => println(b.size)))
    a
  }

  def saveEntities(reader: ParameterizedReader[DriveInfo], sourceFilenameWithPath: String): Unit = {
    val lines = reader.readFile(sourceFilenameWithPath)
    val stations = lines.flatMap(a => Option.option2Iterable(a)
      .map(line => List(
        Station(line.startStationNumber, line.startStation),
        Station(line.endStationNumber, line.endStation)
      )
      ).toList.flatten
    ).distinctBy(_.stationNumber)
    val insertStationsQuery = Tables.stations ++= stations
    DataBase.db.run(insertStationsQuery)
    val drives = lines.flatMap(a => Option.option2Iterable(a)
      .map(line =>
        Drive(
          line.duration,
          line.startDate,
          line.endDate,
          line.startStationNumber,
          line.endStationNumber,
          line.bikeNumber,
          line.memberType
        ),
      )
    )
    val insertDrivesQuery = Tables.drives ++= drives
    val a = DataBase.db.run(insertDrivesQuery)
    Await.ready(a, Duration.Inf)
  }

  def main(args: Array[String]): Unit = {
    val start = System.nanoTime
    execute()
    val end = System.nanoTime
    print((end - start) / 1000000)
  }

  //  def processFile(reader: ParameterizedReader[DriveInfo], reporter: Reporter, sourceFilenamesListWithPath: List[String]): Future[List[Report]] = {
  //    val bikeStatsReportGenerator = new BikeStatsReportGenerator(reporter.directoryPath, reporter.bikeStatsFilename)
  //    val usageStatsReportGenerator = new UsageStatsReportGenerator(reporter.directoryPath, reporter.usageStatsFilename)
  //    val generalStatsReportGenerator = new GeneralStatsReportGenerator(DateRange("2010-09-20 12:26:08", "2010-10-26 12:26:08"), reporter.directoryPath, reporter.generalStatsFilename)
  //    val result: Future[List[Report]] = {
  //      val reports: Future[Seq[(List[(String, Int, Int)], Array[(String, Int)], (Int, Int, Int, Int, Int))]] = Future.sequence(sourceFilenamesListWithPath.par.map(
  //        sourceFilenameListWithPath => Future {
  //          val lines = reader.readFile(sourceFilenameListWithPath)
  //          (bikeStatsReportGenerator.generate(lines), usageStatsReportGenerator.generate(lines), generalStatsReportGenerator.generate(lines))
  //        }).seq)
  //      val bikeStats = reports.map(_.flatMap(_._1).groupBy(_._1).map(map => (map._1, map._2.map(_._2).sum, map._2.map(_._3).sum)).toList)
  //      val usageStats = reports.map(_.flatMap(_._2).groupBy(_._1).map(map => (map._1, map._2.map(_._2).sum)).toArray)
  //      val generalStatsFuture = reports.map(_.map(_._3))
  //      val generalStatsList = Future.sequence(
  //        List(
  //          generalStatsFuture.map(_.map(_._1).sum),
  //          generalStatsFuture.map(_.map(_._2).sum),
  //          generalStatsFuture.map(_.map(_._3).sum),
  //          generalStatsFuture.map(_.map(_._4).sum),
  //          generalStatsFuture.map(_.map(_._5).sum)
  //        )
  //      )
  //      val generalStats = generalStatsList.map(int => {
  //        val iterator = int.iterator
  //        (iterator.next(), iterator.next(), iterator.next(), iterator.next(), iterator.next())
  //      })
  //      Future.sequence(
  //        List(
  //          bikeStats.map(bikeStatsReportGenerator.generateReport),
  //          usageStats.map(usageStatsReportGenerator.generateReport),
  //          generalStats.map(generalStatsReportGenerator.generateReport)
  //        )
  //      )
  //    }
  //    result
  //  }

  def processAll(reader: ParameterizedReader[DriveInfo], reporter: Reporter, sourceFilenamesListWithPath: List[String]): Future[List[Report]] = {
    val bikeStatsReportGenerator = new BikeStatsReportGenerator(reporter.directoryPath, reporter.bikeStatsFilename)
    val usageStatsReportGenerator = new UsageStatsReportGenerator(reporter.directoryPath, reporter.usageStatsFilename)
    val generalStatsReportGenerator = new GeneralStatsReportGenerator(DateRange("2010-09-20 12:26:08", "2010-10-26 12:26:08"), reporter.directoryPath, reporter.generalStatsFilename)
    //    for each file-> process
    val reports = Future.sequence(sourceFilenamesListWithPath.par.map(
      sourceFilenameWithPath =>
        process(bikeStatsReportGenerator,
          usageStatsReportGenerator,
          generalStatsReportGenerator,
          reader,
          sourceFilenameWithPath)
    ).seq)
    //    merge all reports
    merge(bikeStatsReportGenerator, usageStatsReportGenerator, generalStatsReportGenerator, reports).flatMap(a => Future.sequence(a))

  }

  def process(bikeStatsReportGenerator: BikeStatsReportGenerator,
              usageStatsReportGenerator: UsageStatsReportGenerator,
              generalStatsReportGenerator: GeneralStatsReportGenerator,
              reader: ParameterizedReader[DriveInfo],
              sourceFilenameListWithPath: String): Future[(Future[List[BikeReport]], Future[List[UsageReport]], Future[GeneralReport])] = {
    //read all data from file
    val lines = Future(read(reader, sourceFilenameListWithPath))
    //get reports
    lines.map(list => getReport(bikeStatsReportGenerator,
      usageStatsReportGenerator,
      generalStatsReportGenerator,
      list))
  }

  //read
  def read(reader: ParameterizedReader[DriveInfo], sourceFilenameWithPath: String): Future[List[Option[DriveInfo]]] = {
    Future(reader.readFile(sourceFilenameWithPath))
  }

  //get reports
  def getReport(bikeStatsReportGenerator: BikeStatsReportGenerator,
                usageStatsReportGenerator: UsageStatsReportGenerator,
                generalStatsReportGenerator: GeneralStatsReportGenerator,
                lines: Future[List[Option[DriveInfo]]]): (Future[List[BikeReport]], Future[List[UsageReport]], Future[GeneralReport]) = {
    (bikeStatsReportGenerator.generate(lines), usageStatsReportGenerator.generate(lines), generalStatsReportGenerator.generate(lines))
  }

  //merge reports
  def merge(bikeStatsReportGenerator: BikeStatsReportGenerator,
            usageStatsReportGenerator: UsageStatsReportGenerator,
            generalStatsReportGenerator: GeneralStatsReportGenerator,
            reports: Future[Seq[(Future[List[BikeReport]], Future[List[UsageReport]], Future[GeneralReport])]]): Future[List[Future[Report]]] = {
    //merge collections
    val bikeStats: Future[Future[List[BikeReport]]] = reports.map(a =>
      Future.sequence(a.map(_._1))
        .map(_.flatten
          .groupBy(_.bikeNumber)
          .map(map =>
            BikeReport(
              map._1,
              map._2.map(_.countOfDrives).sum,
              map._2.map(_.totalDuration).sum
            )
          ).toList)
    )
    val usageStats: Future[Future[List[UsageReport]]] = reports.map(a =>
      Future.sequence(a.map(_._2))
        .map(_.flatten
          .groupBy(_.monthName)
          .map(map =>
            UsageReport(
              map._1,
              map._2.map(_.countOfDrives).sum
            )
          ).toList)
    )

    val generalStats: Future[Future[GeneralReport]] = reports.map(a =>
      Future.sequence(a.map(_._3))
        .map(g => GeneralReport(
          g.map(_.countOfDrives).sum,
          g.map(_.countOfParseErrors).sum,
          g.map(_.countOfUsagesBetweenDates).sum,
          g.map(_.countOfBicyclesUsedBetweenDates).sum,
          g.map(_.longestDrive).sum,
        )
        )
    )
    //merge general data
    Future.sequence(
      List(
        bikeStats.map(bikeStatsReportGenerator.generateReport),
        usageStats.map(usageStatsReportGenerator.generateReport),
        generalStats.map(generalStatsReportGenerator.generateReport)
      )
    )
  }
}